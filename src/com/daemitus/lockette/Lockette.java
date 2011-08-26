package com.daemitus.lockette;

import com.daemitus.lockette.events.*;
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

    public static final Logger log = Logger.getLogger("Minecraft");
    private PluginManager pm;
    private ConfigManager cm;
    public LogicEngine logic;

    public void onEnable() {

        pm = this.getServer().getPluginManager();

        BlockListener blockListener = new BlockListener(this);
        blockListener.registerEvents(pm);

        EntityListener entityListener = new EntityListener(this);
        entityListener.registerEvents(pm);

        PlayerListener playerListener = new PlayerListener(this);
        playerListener.registerEvents(pm);

        cm = new ConfigManager(this);
        cm.load();

        logic = new LogicEngine(this);
    }

    public void onDisable() {
        logic.shutdown();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ConfigManager.getLocale("console"));
            return true;
        } else {
            Player player = (Player) sender;
            Block block = logic.getSelectedSign(player);
            if (block == null) {
                this.sendMessage(player, "cmd-sign-not-selected", false);
            } else if (args.length < 2) {
                this.sendMessage(player, "cmd-bad-format", false);
            } else {
                try {
                    int lineNum = Integer.valueOf(args[0]);
                    Sign sign = (Sign) block.getState();
                    String text = logic.stripColor(sign.getLine(0));
                    if (lineNum == 1) {
                        this.sendMessage(player, "cmd-identifier-not-changeable", false);
                    } else if ((text.equalsIgnoreCase(ConfigManager.getDefault("signtext-private"))
                                    || text.equalsIgnoreCase(ConfigManager.getLocale("signtext-private")))
                                   && lineNum == 2) {
                        this.sendMessage(player, "cmd-owner-not-changeable", false);
                    } else if (lineNum < 1 || lineNum > 4) {
                        this.sendMessage(player, "cmd-line-num-out-of-range", false);
                    } else {
                        String newText = "";
                        for (int i = 1; i < args.length; i++) {
                            newText += args[i];
                            if (i + 1 < args.length) {
                                newText += " ";
                            }
                        }
                        sign.setLine(lineNum - 1, logic.truncate(newText));
                        sign.update();
                    }
                } catch (NumberFormatException ex) {
                    this.sendMessage(player, "cmd-bad-format", false);
                }
            }
        }
        return true;
    }
    //------------------------------------------------------------------------//
    private final String pluginTag = "Lockette: ";
    private final ChatColor info = ChatColor.GOLD;
    private final ChatColor error = ChatColor.RED;

    public void sendMessage(Player player, String key, boolean isError) {
        String msg = ConfigManager.getLocale(key);
        if (msg.equals(""))
            return;
        player.sendMessage((isError ? error : info) + pluginTag + msg);
    }
    //------------------------------------------------------------------------//
}
