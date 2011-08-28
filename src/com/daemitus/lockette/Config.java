package com.daemitus.lockette;

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

    private final Lockette plugin;
    private final String repo = "https://raw.github.com/daemitus/Lockette/master/src/files/";
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
    public static String signtext_private_locale = "private";
    public static String signtext_moreusers_locale = "more users";
    public static String signtext_everyone_locale = "everyone";
    public static String signtext_timer_locale = "timer";
    public static String console = "";                                  //"This command requires ingame usage";
    public static String console_error_scheduler_start = "";            //"Automatic door scheduler failed to start"
    public static String console_error_scheduler_stop = "";             //"Automatic door scheduler failed to stop"
    public static String cmd_sign_selected = "";                        //"Sign selected, use /lockette <line number> <text>";
    public static String cmd_sign_not_selected = "";                    //"Nothing selected, right click a valid sign first";
    public static String cmd_bad_format = "";                           //"Bad format, use /lockette <line number> <text>";
    public static String cmd_identifier_not_changeable = "";            //"Break and replace to change the identifier on line 1";
    public static String cmd_owner_not_changeable = "";                 //"Break and replace to change the owner on line 2";
    public static String cmd_line_num_out_of_range = "";                //"Bad format, your line number should be 2,3,4";
    public static String msg_admin_break = "";                          //"(Admin) %1$s broke a block owned by %2$s";
    public static String msg_admin_bypass = "";                         //"Bypassed a door owned by %1$s, make sure to shut it";
    public static String msg_admin_signs = "";                          //"Selected a sign owned by %1$s";
    public static String msg_admin_snoop = "";                          //"(Admin) %1$s snooped around in a container owned by %2$s";
    public static String msg_deny_door_access = "";                     //"Access denied";
    public static String msg_deny_container_access = "";                //"Access denied";
    public static String msg_deny_sign_selection = "";                  //"You don't own this sign";
    public static String msg_deny_block_break = "";                     //"You don't own this block";
    public static String msg_deny_chest_expansion = "";                 //"You don't own the adjacent chest";
    public static String msg_deny_door_expansion = "";                  //"You don't own the adjacent door";
    public static String msg_deny_trapdoor_placement = "";              //"You don't own the adjacent hinge block";
    public static String msg_deny_chest_perm = "";                      //"You are not authorized to protect chests";
    public static String msg_deny_dispenser_perm = "";                  //"You are not authorized to protect dispensers";
    public static String msg_deny_furnace_perm = "";                    //"You are not authorized to protect furnaces";
    public static String msg_deny_door_perm = "";                       //"You are not authorized to protect doors";
    public static String msg_deny_trapdoor_perm = "";                   //"You are not authorized to protect trap doors";
    public static String msg_deny_sign_private_nothing_nearby = "";     //"Nothing nearby to protect";
    public static String msg_deny_sign_private_already_owned = "";      //"This block is already protected";
    public static String msg_deny_sign_moreusers_already_owned = "";    //"You don't own this block";
    public static String msg_deny_sign_moreusers_no_private = "";       //"No sign with [Private] nearby";
    public static String msg_warning_player_not_found = "";             //"%1$s is not online, make sure you have the correct name";
    public static String msg_tnt_fizzle = "";                           //"TNT tried to explode too close to a protected block";
    public static String msg_reminder_lock_your_chests = "";            //"Place a sign headed [Private] next to your chest to lock it"
    //------------------------------------------------------------------------//

    public Config(final Lockette plugin) {
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

        timerDoorsAlwaysOn = config.getBoolean("timer-doors-always-on", timerDoorsAlwaysOn);
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
        String language = config.getString("language");

        if (language == null)
            language = "english.yml";
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
        msg_deny_sign_private_nothing_nearby = locale.getString("msg_deny_sign_private_nothing_nearby");//, msg_deny_sign_private_nothing_nearby);
        msg_deny_sign_private_already_owned = locale.getString("msg_deny_sign_private_already_owned", msg_deny_sign_private_already_owned);
        msg_deny_chest_perm = locale.getString("msg_deny_chest_perm", msg_deny_chest_perm);
        msg_deny_dispenser_perm = locale.getString("msg_deny_dispenser_perm", msg_deny_dispenser_perm);
        msg_deny_furnace_perm = locale.getString("msg_deny_furnace_perm", msg_deny_furnace_perm);
        msg_deny_door_perm = locale.getString("msg_deny_door_perm", msg_deny_door_perm);
        msg_deny_trapdoor_perm = locale.getString("msg_deny_trapdoor_perm", msg_deny_trapdoor_perm);
        msg_deny_sign_moreusers_already_owned = locale.getString("msg_deny_sign_moreusers_already_owned", msg_deny_sign_moreusers_already_owned);
        msg_deny_sign_moreusers_no_private = locale.getString("msg_deny_sign_moreusers_no_private", msg_deny_sign_moreusers_no_private);
        msg_warning_player_not_found = locale.getString("msg_warning_player_not_found", msg_warning_player_not_found);
        msg_tnt_fizzle = locale.getString("msg_tnt_fizzle", msg_tnt_fizzle);
        msg_reminder_lock_your_chests = locale.getString("msg_reminder_lock_your_chests", msg_reminder_lock_your_chests);
    }

    private void downloadFile(String filename) {
        //Thanks to Southpaw018 - Cenotaph
        String datafile = plugin.getDataFolder().getPath() + File.separator + filename;
        String repofile = repo + filename;
        try {
            File download = new File(datafile);
            download.createNewFile();
            URL link = new URL(repofile);
            ReadableByteChannel rbc = Channels.newChannel(link.openStream());
            FileOutputStream fos = new FileOutputStream(download);
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            Lockette.logger.log(Level.INFO, "[Lockette] Downloaded file ".concat(datafile));
        } catch (MalformedURLException ex) {
            Lockette.logger.log(Level.WARNING, "[Lockette] Malformed URL ".concat(repofile));
        } catch (FileNotFoundException ex) {
            Lockette.logger.log(Level.WARNING, "[Lockette] File not found ".concat(datafile));
        } catch (IOException ex) {
            Lockette.logger.log(Level.WARNING, "[Lockette] IOError downloading ".concat(repofile));
        }
    }
}
