package com.daemitus.lockette;

import com.daemitus.lockette.events.BlockListener;
import com.daemitus.lockette.events.EntityListener;
import com.daemitus.lockette.events.PlayerListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Lockette extends JavaPlugin {

    public static final Logger logger = Logger.getLogger("Minecraft");
    private PluginManager pm;
    private Config cm;

    public void onEnable() {

        pm = this.getServer().getPluginManager();

        BlockListener blockListener = new BlockListener(this);
        blockListener.registerEvents(pm);

        EntityListener entityListener = new EntityListener(this);
        entityListener.registerEvents(pm);

        PlayerListener playerListener = new PlayerListener(this);
        playerListener.registerEvents(pm);

        cm = new Config(this);
        cm.load();

        boolean started = Util.doorSchedule.start(this);
        if (!started) {
            logger.log(Level.WARNING, Config.console_error_scheduler_start);
        }
    }

    public void onDisable() {
        stopDoorSchedule();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //todo maybe re-imp the /lockette reload command?
        //todo look into cleaning this up
        if (!(sender instanceof Player)) {
            if (!Config.console.equals(""))
                sender.sendMessage("[Lockette] " + Config.console);
            return true;
        } else {
            Player player = (Player) sender;
            Block block = Util.selectedSign.get(player);
            if (block == null) {
                Util.sendMessage(player, Config.cmd_sign_not_selected, ChatColor.YELLOW);
            } else if (args.length < 2) {
                Util.sendMessage(player, Config.cmd_bad_format, ChatColor.RED);
            } else {
                try {
                    int lineNum = Integer.valueOf(args[0]);
                    Sign sign = (Sign) block.getState();
                    String text = Util.stripColor(sign.getLine(0));
                    if (lineNum == 1) {
                        Util.sendMessage(player, Config.cmd_identifier_not_changeable, ChatColor.RED);
                    } else if ((text.equalsIgnoreCase(Config.signtext_private)
                                    || text.equalsIgnoreCase(Config.signtext_private_locale))
                                   && lineNum == 2) {
                        Util.sendMessage(player, Config.cmd_owner_not_changeable, ChatColor.RED);
                    } else if (lineNum < 1 || lineNum > 4) {
                        Util.sendMessage(player, Config.cmd_line_num_out_of_range, ChatColor.RED);
                    } else {
                        String newText = "";
                        for (int i = 1; i < args.length; i++) {
                            newText += args[i];
                            if (i + 1 < args.length) {
                                newText += " ";
                            }
                        }
                        sign.setLine(lineNum - 1, Util.truncate(newText));
                        sign.update();
                    }
                } catch (NumberFormatException ex) {
                    Util.sendMessage(player, Config.cmd_bad_format, ChatColor.RED);
                }
            }
        }
        return true;
    }

    //------------------------------------------------------------------------//
    public void stopDoorSchedule() {
        if (Util.doorSchedule != null) {
            Util.selectedSign.clear();
            boolean stopped = Util.doorSchedule.stop();
            if (!stopped) {
                logger.log(Level.WARNING, Config.console_error_scheduler_stop);
            }
        }
    }
    //------------------------------------------------------------------------//

    /**
     * Check if <name> or [Everyone] is on any of the [Private] or [More Users] signs associated with <block>
     * @param name Name to be checked
     * @param block Block to be checked
     * @return If <name> is authorized to use <block>
     */
    public static boolean isAuthorized(String name, Block block) {
        return Util.isAuthorized(name, block);
    }

    /**
     * Check if <block> is protected by <name> or not
     * @param name Name to be checked
     * @param block Block to be checked
     * @return If <name> owns <block>
     */
    public static boolean isOwner(String name, Block block) {
        return Util.isOwner(name, block);
    }

    /**
     * Retrieves all names authorized to interact with <block>
     * @param block Block to be checked
     * @return A List<String> containing everything on any [Private] or [More Users] signs associated with <block>
     */
    public static List<String> getAllNames(Block block) {
        return Util.getAllNames(block);
    }

    /**
     * Retrieve the block a given wallsign is attached to
     * @param block The wallsign to be checked
     * @return The block that the wallsign in attached to
     */
    public static Block getBlockSignAttachedTo(Block block) {
        return Util.getBlockSignAttachedTo(block);
    }

    /**
     * Retrieves owner of <block>
     * @param block Block to be checked
     * @return The text on the line below [Private] on the sign associated with <block>. "" if unprotected
     */
    public static String getOwnerName(Block block) {
        return Util.getOwnerName(block);
    }

    /**
     * Retrieves the sign block associated with <block>
     * @param block Block to be checked
     * @return The sign block associated with <block>. Null if unprotected
     */
    public static Block getOwnerSign(Block block) {
        return Util.getOwnerSign(block);
    }

    /**
     * Check if <block> is protected or not
     * @param block The block to be checked
     * @return If <block> is owned
     */
    public static boolean isProtected(Block block) {
        return Util.isProtected(block);
    }

    /**
     * Interacts with a (set) of doors, toggling their state if authorized.
     * @param player The player who clicked
     * @param block The block clicked
     * @param override Disregard signs and toggle regardless
     * @return Success or failure
     */
    public static boolean interactDoor(Player player, Block block, boolean override) {
        return Util.interactDoor(player, block, override);
    }

    /**
     * Interacts with a container, opening if authorized
     * @param player The player who clicked
     * @param block The block clicked
     * @param override Disregard signs and toggle regardless
     * @return Success or failure
     */
    public static boolean interactContainer(Player player, Block block, boolean override) {
        return Util.interactContainer(player, block, override);
    }

    /**
     * Interacts with a sign, selecting it for /lockette <line> <text> usage if authorized
     * @param player The player who clicked
     * @param block The block clicked
     * @return Success or failure
     */
    public static boolean interactSign(Player player, Block block) {
        return Util.interactSign(player, block);
    }
}
