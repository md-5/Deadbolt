package com.daemitus.deadbolt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;
import org.bukkit.util.config.Configuration;

public class Config {

    private final Deadbolt plugin;
    private final String repo = "https://raw.github.com/daemitus/Deadbolt/master/src/main/resources/files/";
    //------------------------------------------------------------------------//
    public static boolean adminBreak = true;
    public static boolean adminBypass = true;
    public static boolean adminSign = true;
    public static boolean adminSnoop = true;
    public static boolean explosionProtection = true;
    public static boolean broadcastTNT = false;
    public static int broadcastTNTRadius = 25;
    public static boolean pistonProtection = true;
    public static boolean redstoneProtection = true;
    public static boolean timerDoorsAlwaysOn = false;
    public static int timerDoorsAlwaysOnDelay = 3;
    public static boolean doorSounds = false;
    public static boolean timerDoorSounds = false;
    public static boolean deselectSign = false;
    //------------------------------------------------------------------------//
    public static final String signtext_private = "[private]";
    public static final String signtext_moreusers = "[more users]";
    public static final String signtext_everyone = "[everyone]";
    public static final String signtext_timer = "timer";
    /*  1 */ public static String signtext_private_locale = "private";
    /*  2 */ public static String signtext_moreusers_locale = "more users";
    /*  3 */ public static String signtext_everyone_locale = "everyone";
    /*  4 */ public static String signtext_timer_locale = "timer";
    /*  5 */ public static String console_error_scheduler_start = "Automatic door scheduler failed to start";
    /*  6 */ public static String console_error_scheduler_stop = "Automatic door scheduler failed to stop";
    /*  7 */ public static String cmd_help_editsign = "/deadbolt <line number> <text> - Edit signs on locked containers, right click a sign first to select it";
    /*  8 */ public static String cmd_help_reload = "/deadbolt reload - Reload the config.yml and <language>.yml files";
    /*  9 */ public static String cmd_reload = "Reloading settings...";
    /* 10 */ public static String cmd_sign_updated = "Sign updated";
    /* 11 */ public static String cmd_sign_selected = "Sign selected, use /deadbolt <line number> <text>";
    /* 12 */ public static String cmd_sign_selected_error = "Selected sign has an error. Right click it again";
    /* 13 */ public static String cmd_sign_not_selected = "Nothing selected, right click a valid sign first";
    /* 14 */ public static String cmd_identifier_not_changeable = "Break and replace to change the identifier on line 1";
    /* 15 */ public static String cmd_owner_not_changeable = "Break and replace to change the owner on line 2";
    /* 16 */ public static String cmd_line_num_out_of_range = "Bad format, your line number should be 1, 2, 3 or 4";
    /* 17 */ public static String cmd_command_not_found = "No command found, use \"/deadbolt\" for options";
    /* 18 */ public static String cmd_console_reload = "Deadbolt - Reloading settings...";
    /* 19 */ public static String cmd_console_command_not_found = "Deadbolt - No command found, use \"deadbolt\" for options";
    /* 20 */ public static String msg_admin_break = "(Admin) %1$s broke a block owned by %2$s";
    /* 21 */ public static String msg_admin_bypass = "Bypassed a door owned by %1$s, make sure to shut it";
    /* 22 */ public static String msg_admin_signs = "Selected a sign owned by %1$s";
    /* 23 */ public static String msg_admin_snoop = "(Admin) %1$s snooped around in a container owned by %2$s";
    /* 24 */ public static String msg_deny_door_access = "Access denied";
    /* 25 */ public static String msg_deny_container_access = "Access denied";
    /* 26 */ public static String msg_deny_sign_selection = "You don't own this sign";
    /* 27 */ public static String msg_deny_block_break = "You don't own this block";
    /* 28 */ public static String msg_deny_chest_expansion = "You don't own the adjacent chest";
    /* 29 */ public static String msg_deny_door_expansion = "You don't own the adjacent door";
    /* 30 */ public static String msg_deny_trapdoor_placement = "You don't own the adjacent hinge block";
    /* 31 */ public static String msg_deny_sign_private_nothing_nearby = "Nothing nearby to protect";
    /* 32 */ public static String msg_deny_sign_private_already_owned = "This block is already protected";
    /* 33 */ public static String msg_deny_sign_moreusers_already_owned = "You don't own this block";
    /* 34 */ public static String msg_deny_sign_moreusers_no_private = "No sign with [Private] nearby";
    /* 35 */ public static String msg_deny_chest_perm = "You are not authorized to protect chests";
    /* 36 */ public static String msg_deny_dispenser_perm = "You are not authorized to protect dispensers";
    /* 37 */ public static String msg_deny_furnace_perm = "You are not authorized to protect furnaces";
    /* 38 */ public static String msg_deny_door_perm = "You are not authorized to protect doors";
    /* 39 */ public static String msg_deny_trapdoor_perm = "You are not authorized to protect trap doors";
    /* 40 */ public static String msg_warning_player_not_found = "%1$s is not online, make sure you have the correct name";
    /* 41 */ public static String msg_tnt_fizzle = "TNT tried to explode too close to a protected block";
    /* 42 */ public static String msg_reminder_lock_your_chests = "Place a sign headed [Private] next to your chest to lock it";
    //------------------------------------------------------------------------//

    public Config(final Deadbolt plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads config.yml and associated <language>.yml contained within.
     * It will try to download from GitHub if either is missing and
     * defaults to english.yml if the node is not found.
     */
    public void load() {
        File configFile = new File(plugin.getDataFolder() + File.separator + "config.yml");
        if (!configFile.exists())
            downloadFile("config.yml");
        Configuration config = new Configuration(configFile);
        config.load();

        adminBreak = config.getBoolean("allow-admin-break", adminBreak);
        adminBypass = config.getBoolean("allow-admin-bypass", adminBypass);
        adminSnoop = config.getBoolean("allow-admin-snoop", adminSnoop);
        adminSign = config.getBoolean("allow-admin-sign", adminSign);
        explosionProtection = config.getBoolean("explosion-protection", explosionProtection);
        broadcastTNT = config.getBoolean("broadcast-tnt-fizzle", broadcastTNT);
        broadcastTNTRadius = config.getInt("broadcast-tnt-fizzle-radius", broadcastTNTRadius);
        pistonProtection = config.getBoolean("piston-protection", pistonProtection);
        redstoneProtection = config.getBoolean("redstone-protection", redstoneProtection);
        timerDoorsAlwaysOn = config.getBoolean("timer-doors-always-on", timerDoorsAlwaysOn);
        timerDoorsAlwaysOnDelay = config.getInt("timer-doors-always-on-delay", timerDoorsAlwaysOnDelay);
        doorSounds = config.getBoolean("iron-door-sounds", doorSounds);
        timerDoorSounds = config.getBoolean("timer-door-sounds", timerDoorSounds);
        deselectSign = config.getBoolean("clear-sign-selection", deselectSign);

        String language = config.getString("language", "english.yml");
        File langfile = new File(plugin.getDataFolder() + File.separator + language);
        if (!langfile.exists())
            downloadFile(language);
        loadMessages(langfile);
    }

    private void loadMessages(File langfile) {
        Configuration locale = new Configuration(langfile);
        locale.load();
        signtext_private_locale = locale.getString("signtext_private", signtext_private);
        signtext_private_locale = Util.truncate(
                (signtext_private_locale.startsWith("[") ? "" : "[")
                + signtext_private_locale
                + (signtext_private_locale.endsWith("]") ? "" : "]"));
        signtext_moreusers_locale = locale.getString("signtext_moreusers", signtext_moreusers);
        signtext_moreusers_locale = Util.truncate(
                (signtext_moreusers_locale.startsWith("[") ? "" : "[")
                + signtext_moreusers_locale
                + (signtext_moreusers_locale.endsWith("]") ? "" : "]"));
        signtext_everyone_locale = locale.getString("signtext_everyone", signtext_everyone);
        signtext_everyone_locale = Util.truncate(
                (signtext_everyone_locale.startsWith("[") ? "" : "[")
                + signtext_everyone_locale
                + (signtext_everyone_locale.endsWith("]") ? "" : "]"));

        signtext_timer_locale = locale.getString("signtext_timer", signtext_timer_locale);
        console_error_scheduler_start = locale.getString("console_error_scheduler_start", console_error_scheduler_start);
        console_error_scheduler_stop = locale.getString("console_error_scheduler_stop", console_error_scheduler_stop);
        cmd_help_editsign = locale.getString("cmd_help_editsign", cmd_help_editsign);
        cmd_help_reload = locale.getString("cmd_help_reload", cmd_help_reload);
        cmd_reload = locale.getString("cmd_reload", cmd_reload);
        cmd_sign_updated = locale.getString("cmd_sign_updated", cmd_sign_updated);
        cmd_sign_selected = locale.getString("cmd_sign_selected", cmd_sign_selected);
        cmd_sign_selected_error = locale.getString("cmd_sign_selected_error", cmd_sign_selected_error);
        cmd_sign_not_selected = locale.getString("cmd_sign_not_selected", cmd_sign_not_selected);
        cmd_identifier_not_changeable = locale.getString("cmd_identifier_not_changeable", cmd_identifier_not_changeable);
        cmd_owner_not_changeable = locale.getString("cmd_owner_not_changeable", cmd_owner_not_changeable);
        cmd_line_num_out_of_range = locale.getString("cmd_line_num_out_of_range", cmd_line_num_out_of_range);
        cmd_command_not_found = locale.getString("cmd_command_not_found", cmd_command_not_found);
        cmd_console_reload = locale.getString("cmd_console_reload", cmd_console_reload);
        cmd_console_command_not_found = locale.getString("cmd_console_command_not_found", cmd_console_command_not_found);
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
        msg_deny_sign_moreusers_already_owned = locale.getString("msg_deny_sign_moreusers_already_owned", msg_deny_sign_moreusers_already_owned);
        msg_deny_sign_moreusers_no_private = locale.getString("msg_deny_sign_moreusers_no_private", msg_deny_sign_moreusers_no_private);
        msg_deny_chest_perm = locale.getString("msg_deny_chest_perm", msg_deny_chest_perm);
        msg_deny_dispenser_perm = locale.getString("msg_deny_dispenser_perm", msg_deny_dispenser_perm);
        msg_deny_furnace_perm = locale.getString("msg_deny_furnace_perm", msg_deny_furnace_perm);
        msg_deny_door_perm = locale.getString("msg_deny_door_perm", msg_deny_door_perm);
        msg_deny_trapdoor_perm = locale.getString("msg_deny_trapdoor_perm", msg_deny_trapdoor_perm);
        msg_warning_player_not_found = locale.getString("msg_warning_player_not_found", msg_warning_player_not_found);
        msg_tnt_fizzle = locale.getString("msg_tnt_fizzle", msg_tnt_fizzle);
        msg_reminder_lock_your_chests = locale.getString("msg_reminder_lock_your_chests", msg_reminder_lock_your_chests);
    }

    private void downloadFile(String filename) {
        //Southpaw018 - Cenotaph
        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdirs();
        String datafile = plugin.getDataFolder().getPath() + File.separator + filename;
        String repofile = repo + filename;
        try {
            File download = new File(datafile);
            download.createNewFile();
            URL link = new URL(repofile);
            ReadableByteChannel rbc = Channels.newChannel(link.openStream());
            FileOutputStream fos = new FileOutputStream(download);
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            Deadbolt.logger.log(Level.INFO, "Deadbolt: Downloaded file ".concat(datafile));
        } catch (MalformedURLException ex) {
            Deadbolt.logger.log(Level.WARNING, "Deadbolt: Malformed URL ".concat(repofile));
        } catch (FileNotFoundException ex) {
            Deadbolt.logger.log(Level.WARNING, "Deadbolt: File not found ".concat(datafile));
        } catch (IOException ex) {
            Deadbolt.logger.log(Level.WARNING, "Deadbolt: IOError downloading ".concat(repofile));
        }
    }
}
