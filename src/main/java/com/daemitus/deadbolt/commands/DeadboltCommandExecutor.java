package com.daemitus.deadbolt.commands;

import com.daemitus.deadbolt.Conf;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Perm;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

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
            player.sendMessage(ChatColor.RED + Conf.cmd_help_editsign);
            if (player.hasPermission(Perm.command_reload))
                player.sendMessage(ChatColor.RED + Conf.cmd_help_reload);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            Conf.sendMessage(player, Conf.cmd_reload, ChatColor.RED);
            new Conf(plugin);
            return true;
        }

        try {
            int line = Integer.valueOf(args[0]);
            if (line < 1 || line > 4) {
                Conf.sendMessage(player, Conf.cmd_line_num_out_of_range, ChatColor.RED);
                return true;
            }
            Block block = Conf.selectedSign.get(player);
            if (block == null) {
                Conf.sendMessage(player, Conf.cmd_sign_not_selected, ChatColor.RED);
                return true;
            }
            if (!block.getType().equals(Material.WALL_SIGN)) {
                Conf.sendMessage(player, Conf.cmd_sign_selected_error, ChatColor.RED);
                Conf.selectedSign.remove(player);
                return true;
            }

            line--;
            Sign sign = (Sign) block.getState();
            String lines[] = sign.getLines();

            String text = "";
            for (int i = 1; i < args.length; i++)
                text += args[i] + (i + 1 < args.length ? " " : "");
            if (player.hasPermission(Perm.user_color))
                text = Conf.replaceColor(text);
            text = Conf.truncate(text, 15);
            if (line == 0) {
                if (Conf.stripColor(lines[0]).equalsIgnoreCase(Conf.stripColor(text))) {
                    lines[0] = text;
                } else {
                    Conf.sendMessage(player, Conf.cmd_identifier_not_changeable, ChatColor.RED);
                    return true;
                }
            } else if (line == 1 && Conf.isPrivate(lines[0])) {
                if (Conf.stripColor(lines[1]).equalsIgnoreCase(Conf.stripColor(text))) {
                    lines[1] = text;
                } else {
                    Conf.sendMessage(player, Conf.cmd_owner_not_changeable, ChatColor.RED);
                    return true;
                }
            } else {
                lines[line] = text;
            }
            if (Conf.deselectSign)
                Conf.selectedSign.remove(player);

            boolean isPrivate = Conf.isPrivate(lines[0]);
            boolean isMoreUsers = Conf.isMoreUsers(lines[0]);
            if (isPrivate)
                for (int i = 0; i < 4; i++)
                    lines[i] = Conf.default_color_private[i] + lines[i];
            else if (isMoreUsers)
                for (int i = 0; i < 4; i++)
                    lines[i] = Conf.default_color_moreusers[i] + lines[i];
            Conf.setLines(sign, lines);
            Conf.sendMessage(player, Conf.cmd_sign_updated, ChatColor.GOLD);
            return true;
        } catch (NumberFormatException ex) {
        }

        Conf.sendMessage(player, Conf.cmd_command_not_found, ChatColor.RED);
        return true;
    }

    private boolean onConsoleCommand(CommandSender sender, Command command, String label, String[] args) {
        int arg = args.length;
        if (arg == 0) {
            sender.sendMessage("Deadbolt v" + plugin.getDescription().getVersion() + " options: reload");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("Deadbolt: " + Conf.cmd_console_reload);
            new Conf(plugin);
            return true;
        }

        sender.sendMessage("Deadbolt: " + Conf.cmd_console_command_not_found);
        return true;
    }
}
