package com.daemitus.lockette;

import org.bukkit.ChatColor;

public class ConfigManager {

    private final Lockette plugin;
    //------------------------------------------------------------------------//
    public final String pluginTag = "Lockette";
    public final ChatColor color_info = ChatColor.GOLD;
    public final ChatColor color_error = ChatColor.RED;
    //------------------------------------------------------------------------//
    public final String ingame_sign_primary = "[private]";
    public final String ingame_sign_moreusers = "[more users]";
    public final String ingame_sign_everyone = "[everyone]";
    public final String ingame_sign_timer = "timer";
    public final String msg_user_denied_door = "Access denied";
    public final String msg_user_denied_container = "Access denied";
    public final String msg_user_denied_sign = "You don't own this sign";
    public final String msg_user_denied_break = "You don't own this block";
    public final String msg_user_denied_chest_place = "You don't own the adjacent chest";
    public final String msg_user_denied_door_place = "You don't own the adjacent door";
    public final String msg_user_denied_trapdoor_place = "You don't own the adjacent hinge block";
    public final String msg_user_denied_sign_place_primary = "This block is already protected";
    public final String msg_user_denied_sign_place_moreusers = "You don't own this block";
    public final String msg_user_sign_selected = "Sign selected, use /lockette <line number> <text>";
    public final String msg_tnt_fizzle = "TNT tried to explode too close to a protected block";
    public final String err_msg_command_console = "Lockette: You cannot use this command outside of the game";
    public final String err_msg_command_format = "Bad format, use /lockette <line number> <text>";
    public final String err_msg_command_owner = "Break and replace to change the owner on line 2";
    public final String err_msg_command_user_sign_not_selected = "Nothing selected, right click a valid sign first";
    public final String err_msg_command_cannot_change_sign_identifier = "Break and replace to change the identifier on line 1";
    public final String err_msg_command_line_number_out_of_range = "Bad format, your line number should be 2,3,4";
    
    //------------------------------------------------------------------------//
    public String ingame_sign_primary_locale = ingame_sign_primary;
    public String ingame_sign_moreusers_locale = ingame_sign_moreusers;
    public String ingame_sign_everyone_locale = ingame_sign_everyone;
    public String ingame_sign_timer_locale = ingame_sign_timer;
    public String msg_user_denied_door_locale = msg_user_denied_door;
    public String msg_user_denied_container_locale = msg_user_denied_container;
    public String msg_user_denied_sign_locale = msg_user_denied_sign;
    public String msg_user_denied_break_locale = msg_user_denied_break;
    public String msg_user_denied_chest_place_locale = msg_user_denied_chest_place;
    public String msg_user_denied_door_place_locale = msg_user_denied_door_place;
    public String msg_user_denied_trapdoor_place_locale = msg_user_denied_trapdoor_place;
    public String msg_user_denied_sign_place_primary_locale = msg_user_denied_sign_place_primary;
    public String msg_user_denied_sign_place_moreusers_locale = msg_user_denied_sign_place_moreusers;
    public String msg_user_sign_selected_locale = msg_user_sign_selected;
    public String msg_tnt_fizzle_locale = msg_tnt_fizzle;
    public String err_msg_command_console_locale = err_msg_command_console;
    public String err_msg_command_format_locale = err_msg_command_format;
    public String err_msg_command_owner_locale = err_msg_command_owner;
    public String err_msg_command_user_sign_not_selected_locale = err_msg_command_user_sign_not_selected;
    public String err_msg_command_cannot_change_sign_identifier_locale = err_msg_command_cannot_change_sign_identifier;
    public String err_msg_command_line_number_out_of_range_locale = err_msg_command_line_number_out_of_range;
    //------------------------------------------------------------------------//
    public final String patternStripColor = "(?i)§[0-9a-zA-Z]";
    public final String patternNormalTooLong = ".{16,}";
    public final String patternBracketTooLong = "\\[.{14,}\\]";
    public final String patternTimer = "\\[.{1,11}:[123]\\]";
    //------------------------------------------------------------------------//
    public boolean setting_explosion_protection = true;
    public boolean setting_broadcast_tnt_fizzle = true;
    public int setting_broadcast_tnt_fizzle_radius = 25;


    //------------------------------------------------------------------------//
    public ConfigManager(final Lockette plugin) {
        this.plugin = plugin;
    }

    public void loadConfig(boolean reload) {
    }
}
