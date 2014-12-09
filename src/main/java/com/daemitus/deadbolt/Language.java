package com.daemitus.deadbolt;

import com.md_5.config.AnnotatedConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.block.Sign;

import java.util.regex.Pattern;

@Data
@EqualsAndHashCode(callSuper = false)
public class Language extends AnnotatedConfig {

    public transient Pattern p_signtext_private;
    public transient Pattern p_signtext_moreusers;
    public transient Pattern p_signtext_everyone;
    public transient Pattern p_signtext_timer;
    //
    public transient String d_signtext_private = "private";
    public transient String d_signtext_moreusers = "more users";
    public transient String d_signtext_everyone = "everyone";
    public transient String d_signtext_timer = "timer";
    //
    public String signtext_private = "private";
    public String signtext_moreusers = "more users";
    public String signtext_everyone = "everyone";
    public String signtext_timer = "timer";
    public String cmd_help_editsign = "/deadbolt <line number> <text> - Edit signs on locked containers, right click a sign first to select it";
    public String cmd_help_reload = "/deadbolt reload - Reload the config.yml and <language>.yml files";
    public String cmd_help_fix = "/deadbolt fix - toggle a single block";
    public String cmd_help_fixAll = "/deadbolt fixall - toggle all related blocks";
    public String cmd_reload = "Reloading settings...";
    public String cmd_fix_notowned = "You don't own that block";
    public String cmd_fix_bad_type = "You can only fix blocks that open and close";
    public String cmd_sign_updated = "Sign updated";
    public String cmd_sign_selected = "Sign selected, use /deadbolt <line number> <text>";
    public String cmd_sign_selected_error = "Selected sign has an error. Right click it again";
    public String cmd_sign_not_selected = "Nothing selected, right click a valid sign first";
    public String cmd_identifier_not_changeable = "The identifier on line 1 is not changeable, except for color.";
    public String cmd_owner_not_changeable = "The owner on line 2 is not changeable, except for color.";
    public String cmd_line_num_out_of_range = "Bad format, your line number should be 1,2,3,4";
    public String cmd_command_not_found = "No command found, use \"/deadbolt\" for options";
    public String cmd_console_reload = "Deadbolt - Reloading settings...";
    public String cmd_console_command_not_found = "Deadbolt - No command found, use \"deadbolt\" for options";
    //
    public String msg_admin_break = "(Admin) %1$s broke a block owned by %2$s";
    public String msg_admin_bypass = "(Admin) Warning, this door is owned by %1$s, make sure to shut it";
    public String msg_admin_sign_placed = "(Admin) Warning, this block is owned by %1$s";
    public String msg_admin_sign_selection = "(Admin) Warning, selected a sign owned by %1$s";
    public String msg_admin_block_fixed = "(Admin) Warning, fixed a block owned by %1$s";
    public String msg_admin_container = "(Admin) %1$s opened a container owned by %2$s";
    public String msg_admin_warning_player_not_found = "%1$s is not online, make sure you have the correct name";
    public String msg_deny_access_door = "Access denied";
    public String msg_deny_access_container = "Access denied";
    public String msg_deny_sign_selection = "You don't own this sign";
    public String msg_deny_block_break = "You don't own this block";
    public String msg_deny_container_expansion = "You don't own the adjacent container";
    public String msg_deny_door_expansion = "You don't own the adjacent door";
    public String msg_deny_trapdoor_expansion = "You don't own the adjacent trapdoor/hinge block";
    public String msg_deny_fencegate_expansion = "You don't own the adjacent fencegate/nearby block";
    public String msg_deny_sign_private_nothing_nearby = "Nothing nearby to protect";
    public String msg_deny_sign_private_already_owned = "This block is already protected";
    public String msg_deny_sign_moreusers_already_owned = "You don't own this block";
    public String msg_deny_sign_moreusers_no_private = "No sign with [Private] nearby";
    public String msg_deny_sign_quickplace = "You cant protect this block, %1$s already has";
    public String msg_deny_block_perm = "You do not have permission to protect this type of block";
    public String msg_reminder_lock_your_chests = "Place a sign headed [Private] next to your block to lock it";
    public String msg_auto_expire_owner_x_days = "Expires if %1$s is offline %2$s more days";
    public String msg_auto_expire_expired = "This protection has expired";
    public String msg_hopper = "You cannot place hoppers under this block as you do not own it!";

    public boolean isPrivate(String line) {
        return p_signtext_private.matcher(line).matches();
    }

    public boolean isMoreUsers(String line) {
        return p_signtext_moreusers.matcher(line).matches();
    }

    public boolean isEveryone(String line) {
        return p_signtext_everyone.matcher(line).matches();
    }

    public boolean isTimer(String line) {
        return p_signtext_timer.matcher(line).matches();
    }

    public int getTimer(String line) {
        try {
            return Integer.parseInt(p_signtext_timer.matcher(line).replaceAll("$2"));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    public boolean isValidWallSign(Sign signState) {
        String line = Util.getLine(signState, 0);
        return isPrivate(line) || isMoreUsers(line);
    }
}
