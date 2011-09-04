package com.daemitus.deadbolt.commands;

import com.daemitus.deadbolt.Config;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Perm;
import com.daemitus.deadbolt.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeadboltCommandExecutor implements CommandExecutor {

    private final Deadbolt plugin;

    public DeadboltCommandExecutor(final Deadbolt plugin) {
        this.plugin = plugin;
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
            player.sendMessage(ChatColor.RED + "Deadbolt v" + plugin.getDescription().getVersion());
            player.sendMessage(ChatColor.RED + Config.cmd_help_editsign);
            if (player.hasPermission(Perm.command_reload))
                player.sendMessage(ChatColor.RED + Config.cmd_help_reload);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            Util.sendMessage(player, Config.cmd_reload, ChatColor.RED);
            plugin.cm.load();
            return true;
        }

        try {
            int line = Integer.valueOf(args[0]);
            if (line < 1 || line > 4) {
                Util.sendMessage(player, Config.cmd_line_num_out_of_range, ChatColor.RED);
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

            Sign sign = (Sign) block.getState();
            String ident = Util.stripColor(sign.getLine(0));
            String name = Util.stripColor(sign.getLine(1));
            String text = "";
            for (int i = 1; i < args.length; i++)
                text += args[i] + (i + 1 < args.length ? " " : "");
            if (player.hasPermission(Perm.user_color))
                text = text.replaceAll(Util.patternFindColor, Util.patternReplaceColor);
            text = Util.truncate(text);
            if (line == 1) {
                if (ident.equalsIgnoreCase(Util.stripColor(text))) {
                    sign.setLine(line - 1, text);
                } else {
                    Util.sendMessage(player, Config.cmd_identifier_not_changeable, ChatColor.RED);
                    return true;
                }
            } else if (line == 2 && (name.equalsIgnoreCase(Config.signtext_private) || name.equalsIgnoreCase(Config.signtext_private_locale))) {
                if (ident.equalsIgnoreCase(Util.stripColor(text))) {
                    sign.setLine(line - 1, text);
                } else {
                    Util.sendMessage(player, Config.cmd_owner_not_changeable, ChatColor.RED);
                    return true;
                }
            } else {
                sign.setLine(line - 1, text);
            }
            sign.update(true);
            if (Config.deselectSign)
                Util.selectedSign.remove(player);
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
            sender.sendMessage("Deadbolt v" + plugin.getDescription().getVersion() + " options: reload");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("Deadbolt: " + Config.cmd_console_reload);
            plugin.cm.load();
            return true;
        }

        sender.sendMessage("Deadbolt: " + Config.cmd_console_command_not_found);
        return true;
    }
}
