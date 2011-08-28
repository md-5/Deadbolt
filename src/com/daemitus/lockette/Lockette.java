package com.daemitus.lockette;

import com.daemitus.lockette.events.BlockListener;
import com.daemitus.lockette.events.EntityListener;
import com.daemitus.lockette.events.PlayerListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
        if (sender instanceof Player)
            return onPlayerCommand((Player) sender, command, label, args);
        else
            return onConsoleCommand(sender, command, label, args);
    }

    private boolean onPlayerCommand(Player player, Command command, String label, String[] args) {
        int arg = args.length;

        if (arg == 0) {
            player.sendMessage(ChatColor.RED + "Lockette v" + this.getDescription().getVersion());
            player.sendMessage(ChatColor.RED + Config.cmd_help_editsign);
            if (player.hasPermission(Perm.command_reload))
                player.sendMessage(ChatColor.RED + Config.cmd_help_reload);
            return true;

        }

        if (args[0].matches("reload")) {
            Util.sendMessage(player, Config.cmd_reload, ChatColor.RED);
            cm.load();
            return true;
        }

        Block block = Util.selectedSign.get(player);
        if (block == null) {
            Util.sendMessage(player, Config.cmd_sign_not_selected, ChatColor.YELLOW);
            return true;
        }

        if (!block.getType().equals(Material.WALL_SIGN)) {
            Util.sendMessage(player, Config.cmd_sign_selected_error, ChatColor.RED);
            Util.selectedSign.remove(player);
            return true;
        }

        try {
            Sign sign = (Sign) block.getState();
            String ident = Util.stripColor(sign.getLine(0));
            boolean isPrivate = ident.equalsIgnoreCase(Config.signtext_private) || ident.equalsIgnoreCase(Config.signtext_private_locale);
            int line = Integer.valueOf(args[0]);

            if (line < 1 || line > 4) {
                Util.sendMessage(player, Config.cmd_line_num_out_of_range, ChatColor.RED);
                return true;
            }
            if (line == 1) {
                Util.sendMessage(player, Config.cmd_identifier_not_changeable, ChatColor.RED);
                return true;
            }
            if (line == 2 && isPrivate) {
                Util.sendMessage(player, Config.cmd_owner_not_changeable, ChatColor.RED);
                return true;
            }

            String newtext = "";
            for (int i = 1; i < args.length; i++) {
                newtext += args[i];
                if (i + 1 < args.length) {
                    newtext += " ";
                }
            }
            sign.setLine(line - 1, Util.truncate(newtext));
            sign.update(true);
            Util.sendMessage(player, Config.cmd_sign_updated, ChatColor.GOLD);
            return true;

        } catch (NumberFormatException ex) {
        }

        Util.sendMessage(player, Config.cmd_command_not_found, ChatColor.RED);
        return true;
    }

    private boolean onConsoleCommand(CommandSender sender, Command command, String label, String[] args) {
        int arg = args.length;
        if (arg == 0) {
            sender.sendMessage("Lockette v" + this.getDescription().getVersion() + " options: reload");
        } else if (arg == 1 && args[0].equals("reload")) {
            sender.sendMessage(Config.cmd_console_reload);
            cm.load();
        } else {
            sender.sendMessage(Config.cmd_console_command_not_found);
        }
        return true;
    }

    private void stopDoorSchedule() {
        Util.selectedSign.clear();
        if (!Util.doorSchedule.stop()) {
            logger.log(Level.WARNING, Config.console_error_scheduler_stop);
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
