package com.daemitus.lockette;

import com.daemitus.lockette.events.*;
import java.util.logging.Logger;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Lockette extends JavaPlugin {

    public static final Logger log = Logger.getLogger("Minecraft");
    public PluginManager pm;
    public ConfigManager cm;
    public LogicEngine logic;

    public void onEnable() {

        pm = this.getServer().getPluginManager();

        BlockListener blockListener = new BlockListener(this);
        blockListener.registerEvents();

        EntityListener entityListener = new EntityListener(this);
        entityListener.registerEvents();

        PlayerListener playerListener = new PlayerListener(this);
        playerListener.registerEvents();

        cm = new ConfigManager(this);
        cm.loadConfig(false);

        logic = new LogicEngine(this);
    }

    public void onDisable() {
        logic.shutdown();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(this.cm.err_msg_command_console_locale);
            return true;
        } else {
            Player player = (Player) sender;
            Block block = logic.getSelectedSign(player);
            if (block == null) {
                logic.sendInfoMessage(player, cm.err_msg_command_user_sign_not_selected_locale);
            } else if (args.length < 2) {
                logic.sendInfoMessage(player, cm.err_msg_command_format_locale);
            } else {
                try {
                    int lineNum = Integer.valueOf(args[0]);
                    Sign sign = (Sign) block.getState();
                    String text = logic.stripColor(sign.getLine(0));
                    if (lineNum == 1) {
                        logic.sendInfoMessage(player, cm.err_msg_command_cannot_change_sign_identifier_locale);
                    } else if ((text.equalsIgnoreCase(cm.ingame_sign_primary) || text.equalsIgnoreCase(cm.ingame_sign_primary_locale)) && lineNum == 2) {
                        logic.sendInfoMessage(player, cm.err_msg_command_owner_locale);
                    } else if (lineNum < 1 || lineNum > 4) {
                        logic.sendInfoMessage(player, cm.err_msg_command_line_number_out_of_range);
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
                    logic.sendInfoMessage(player, cm.err_msg_command_format_locale);
                }
            }
        }
        return true;
    }
}
