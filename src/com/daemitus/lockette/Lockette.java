package com.daemitus.lockette;

import com.daemitus.lockette.events.BlockListener;
import com.daemitus.lockette.events.EntityListener;
import com.daemitus.lockette.events.PlayerListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
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
    private static DoorSchedule doorSchedule;
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

        doorSchedule = new DoorSchedule(this);
        doorSchedule.start();
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
            Block block = Util.getSelectedSign(player);
            if (block == null) {
                this.sendMessage(player, Config.cmd_sign_not_selected, ChatColor.YELLOW);
            } else if (args.length < 2) {
                this.sendMessage(player, Config.cmd_bad_format, ChatColor.RED);
            } else {
                try {
                    int lineNum = Integer.valueOf(args[0]);
                    Sign sign = (Sign) block.getState();
                    String text = Util.stripColor(sign.getLine(0));
                    if (lineNum == 1) {
                        this.sendMessage(player, Config.cmd_identifier_not_changeable, ChatColor.RED);
                    } else if ((text.equalsIgnoreCase(Config.signtext_private)
                                    || text.equalsIgnoreCase(Config.signtext_private_locale))
                                   && lineNum == 2) {
                        this.sendMessage(player, Config.cmd_owner_not_changeable, ChatColor.RED);
                    } else if (lineNum < 1 || lineNum > 4) {
                        this.sendMessage(player, Config.cmd_line_num_out_of_range, ChatColor.RED);
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
                    this.sendMessage(player, Config.cmd_bad_format, ChatColor.RED);
                }
            }
        }
        return true;
    }
    //------------------------------------------------------------------------//
    private final String pluginTag = "Lockette: ";

    public void sendMessage(Player player, String msg, ChatColor color) {
        if (msg.equals(""))
            return;
        player.sendMessage(color + pluginTag + msg);
    }

    public void sendBroadcast(String perm, String msg, ChatColor color) {
        if (msg.equals(""))
            return;
        for (Player player : this.getServer().getOnlinePlayers()) {
            if (player.hasPermission(perm))
                player.sendMessage(color + pluginTag + msg);
        }
    }
    //------------------------------------------------------------------------//

    public static void scheduleDoor(Set<Block> doorBlocks, int delay) {
        doorSchedule.add(doorBlocks, delay);
    }

    public static void stopDoorSchedule() {
        if (doorSchedule != null) {
            Util.clearSelectedSigns();
            doorSchedule.stop();
        }
    }
}
