package com.daemitus.lockette;

import java.io.File;
import org.bukkit.util.config.Configuration;

public class Config {

    private final Lockette plugin;
    //------------------------------------------------------------------------//
    public static boolean timerDoorsAlwaysOn = false;
    public static int timerDoorsAlwaysOnDelay = 3;
    public static boolean explosionProtection = true;
    public static boolean broadcastTNT = true;
    public static int broadcastTNTRadius = 25;
    public static boolean pistonProtection = true;
    public static boolean redstoneProtection = true;
    public static boolean adminBreak = true;
    public static boolean adminBypass = true;
    public static boolean adminSign = true;
    public static boolean adminSnoop = true;
    public static boolean usePermissions = true;
    public static boolean useOpList = false;
    //------------------------------------------------------------------------//
    public static final String signtext_private = "[private]";
    public static final String signtext_moreusers = "[more users]";
    public static final String signtext_everyone = "[everyone]";
    public static final String signtext_timer = "timer";
    public static String signtext_private_locale = signtext_private;
    public static String signtext_moreusers_locale = signtext_moreusers;
    public static String signtext_everyone_locale = signtext_everyone;
    public static String signtext_timer_locale = signtext_timer;
    public static String console = "Lockette: This command requires ingame usage";
    public static String cmd_sign_selected = "Sign selected, use /lockette <line number> <text>";
    public static String cmd_sign_not_selected = "Nothing selected, right click a valid sign first";
    public static String cmd_bad_format = "Bad format, use /lockette <line number> <text>";
    public static String cmd_identifier_not_changeable = "Break and replace to change the identifier on line 1";
    public static String cmd_owner_not_changeable = "Break and replace to change the owner on line 2";
    public static String cmd_line_num_out_of_range = "Bad format, your line number should be 2,3,4";
    public static String msg_admin_break = "%1$s broke a block owned by %2$s";
    public static String msg_admin_bypass = "Bypassed a door owned by %1$s, make sure to shut it";
    public static String msg_admin_signs = "Selected a sign owned by %1$s";
    public static String msg_admin_snoop = "(Admin) %1$s snooped around in a container owned by %2$s";
    public static String msg_deny_door_access = "Access denied";
    public static String msg_deny_container_access = "Access denied";
    public static String msg_deny_sign_selection = "You don't own this sign";
    public static String msg_deny_block_break = "You don't own this block";
    public static String msg_deny_chest_expansion = "You don't own the adjacent chest";
    public static String msg_deny_door_expansion = "You don't own the adjacent door";
    public static String msg_deny_trapdoor_placement = "You don't own the adjacent hinge block";
    public static String msg_deny_sign_private_nothing_nearby = "Nothing nearby to protect";
    public static String msg_deny_sign_private_already_owned = "This block is already protected";
    public static String msg_deny_chest_perm = "You are not authorized to protect chests";
    public static String msg_deny_dispenser_perm = "You are not authorized to protect dispensers";
    public static String msg_deny_furnace_perm = "You are not authorized to protect furnaces";
    public static String msg_deny_door_perm = "You are not authorized to protect doors";
    public static String msg_deny_trapdoor_perm = "You are not authorized to protect trap doors";
    public static String msg_deny_sign_moreusers_already_owned = "You don't own this block";
    public static String msg_deny_sign_moreusers_no_private = "No sign with [Private] nearby";
    public static String msg_warning_player_not_found = "%1$s is not online, make sure you have the correct name";
    public static String msg_tnt_fizzle = "TNT tried to explode too close to a protected block";
    //------------------------------------------------------------------------//

    public Config(final Lockette plugin) {
        this.plugin = plugin;
    }

    public void load() {
        Configuration config = plugin.getConfiguration();
        config.load();
        pistonProtection = config.getBoolean("timer-doors-always-on", pistonProtection);
        timerDoorsAlwaysOnDelay = config.getInt("timer-doors-always-on-delay", timerDoorsAlwaysOnDelay);
        explosionProtection = config.getBoolean("explosion-protection", explosionProtection);
        broadcastTNT = config.getBoolean("broadcast-tnt-fizzle", broadcastTNT);
        broadcastTNTRadius = config.getInt("broadcast-tnt-fizzle-radius", broadcastTNTRadius);
        pistonProtection = config.getBoolean("piston-protection", pistonProtection);
        redstoneProtection = config.getBoolean("redstone-protection", redstoneProtection);
        adminBreak = config.getBoolean("allow-admin-break", adminBreak);
        adminBypass = config.getBoolean("allow-admin-bypass", adminBypass);
        adminSnoop = config.getBoolean("allow-admin-snoop", adminSnoop);
        adminSign = config.getBoolean("allow-admin-sign", adminSign);
        usePermissions = config.getBoolean("use-Permissions", usePermissions);
        useOpList = config.getBoolean("use-OP-list", useOpList);

        config.save();
        String language = config.getString("language");

        if (language == null)
            language = "default";
        if (!language.equals("default")) {
            loadMessages(language);
        }
    }

    public void loadMessages(String language) {
        Configuration locale = new Configuration(new File(plugin.getDataFolder() + File.separator + language + ".yml"));
        signtext_private_locale = locale.getString("signtext_private", signtext_private);
        signtext_moreusers_locale = locale.getString("signtext_moreusers", signtext_moreusers);
        signtext_everyone_locale = locale.getString("signtext_everyone", signtext_everyone);
        signtext_timer_locale = locale.getString("signtext_timer", signtext_timer);
        console = locale.getString("console", console);
        cmd_sign_selected = locale.getString("cmd_sign_selected", cmd_sign_selected);
        cmd_sign_not_selected = locale.getString("cmd_sign_not_selected", cmd_sign_not_selected);
        cmd_bad_format = locale.getString("cmd_bad_format", cmd_bad_format);
        cmd_identifier_not_changeable = locale.getString("cmd_identifier_not_changeable", cmd_identifier_not_changeable);
        cmd_owner_not_changeable = locale.getString("cmd_owner_not_changeable", cmd_owner_not_changeable);
        cmd_line_num_out_of_range = locale.getString("cmd_line_num_out_of_range", cmd_line_num_out_of_range);
        msg_admin_break = locale.getString("msg_admin_break", msg_admin_break);
        msg_admin_bypass = locale.getString("msg_admin_bypass", msg_admin_bypass);
        msg_admin_signs = locale.getString("msg_admin_signs", msg_admin_signs);
        msg_admin_snoop = locale.getString("msg_admin_snoop", msg_admin_snoop);
        msg_deny_door_access = locale.getString("msg_deny_door_access", msg_deny_door_access);
        msg_deny_container_access = locale.getString("msg_deny_container_access", msg_deny_container_access);
        msg_deny_sign_selection = locale.getString("msg_deny_sign_selection", msg_deny_sign_selection);
        msg_deny_block_break = locale.getString("msg_deny_block_break", msg_deny_block_break);
        msg_deny_chest_expansion = locale.getString("msg_deny_chest_expansion", msg_deny_chest_expansion);
        msg_deny_door_expansion = locale.getString("msg_deny_door_expansion", msg_deny_door_expansion);
        msg_deny_trapdoor_placement = locale.getString("msg_deny_trapdoor_placement", msg_deny_trapdoor_placement);
        msg_deny_sign_private_nothing_nearby = locale.getString("msg_deny_sign_private_nothing_nearby", msg_deny_sign_private_nothing_nearby);
        msg_deny_sign_private_already_owned = locale.getString("msg_deny_sign_private_already_owned", msg_deny_sign_private_already_owned);
        msg_deny_chest_perm = locale.getString("msg_deny_chest_perm", msg_deny_chest_perm);
        msg_deny_dispenser_perm = locale.getString("msg_deny_dispenser_perm", msg_deny_dispenser_perm);
        msg_deny_furnace_perm = locale.getString("msg_deny_furnace_perm", msg_deny_furnace_perm);
        msg_deny_door_perm = locale.getString("msg_deny_door_perm", msg_deny_door_perm);
        msg_deny_trapdoor_perm = locale.getString("msg_deny_trapdoor_perm", msg_deny_trapdoor_perm);
        msg_deny_sign_moreusers_already_owned = locale.getString("msg_deny_sign_moreusers_already_owned", msg_deny_sign_moreusers_already_owned);
        msg_deny_sign_moreusers_no_private = locale.getString("msg_deny_sign_moreusers_no_private", msg_deny_sign_moreusers_no_private);
        msg_warning_player_not_found = locale.getString("msg_warning_player_not_found ", msg_warning_player_not_found);
        msg_tnt_fizzle = locale.getString("msg_tnt_fizzle", msg_tnt_fizzle);
        locale.save();
    }
}
