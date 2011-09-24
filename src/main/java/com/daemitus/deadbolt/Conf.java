package com.daemitus.deadbolt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

public final class Conf {

    private final Deadbolt plugin;
    //------------------------------------------------------------------------//
    private final String REPO = "https://raw.github.com/daemitus/Deadbolt/master/src/main/resources/files/";
    //------------------------------------------------------------------------//
    public static final Set<BlockFace> CARDINAL_FACES = EnumSet.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);
    public static final Set<BlockFace> VERTICAL_FACES = EnumSet.of(BlockFace.UP, BlockFace.DOWN);
    //------------------------------------------------------------------------//
    public static Set<Player> reminder = new HashSet<Player>();
    public static Map<Player, Block> selectedSign = new HashMap<Player, Block>();
    //------------------------------------------------------------------------//
    public static Pattern pattern_signtext_private;
    public static Pattern pattern_signtext_moreusers;
    public static Pattern pattern_signtext_everyone;
    public static Pattern pattern_signtext_timer;
    private static final String DEFAULT_SIGNTEXT_PRIVATE = "private";
    private static final String DEFAULT_SIGNTEXT_MOREUSERS = "more users";
    private static final String DEFAULT_SIGNTEXT_EVERYONE = "everyone";
    private static final String DEFAULT_SIGNTEXT_TIMER = "timer";
    //------------------------------------------------------------------------//
    private static final String TAG = "Deadbolt: ";
    //------------------------------------------------------------------------//    
    private static final Pattern COLORPATTERN = Pattern.compile("§[0-9a-fA-F]");
    private static final Pattern TWOCOLORPATTERN = Pattern.compile("(§[0-9a-fA-F])(\\s*)(§[0-9a-fA-F])");
    private static final Pattern PRECOLORPATTERN = Pattern.compile("(\\&)([0-9a-fA-F])");
    /* 20,21,22,23 */ public static String[] default_color_private = {"0", "0", "0", "0"};
    /* 24,25,26,27 */ public static String[] default_color_moreusers = {"0", "0", "0", "0"};
    //------------------------------------------------------------------------//
    /*  3 */ public static boolean groupContainers = true;
    /*  5 */ public static boolean pistonProtection = true;
    /*  6 */ public static boolean redstoneProtection = true;
    /*  9 */ public static boolean explosionProtection = true;
    /* 10 */ public static boolean broadcastTNT = true;
    /* 11 */ public static int broadcastTNTRadius = 25;   
    /* 29 */ public static int defaultTimer = 0;
    /* 31 */ public static boolean silentDoorSounds = false;
    /* 32 */ public static boolean timedDoorSounds = false;
    /* 34 */ public static boolean deselectSign = false;
    //------------------------------------------------------------------------//
    /*  1 */ public static String signtext_private;
    /*  2 */ public static String signtext_moreusers;
    /*  3 */ public static String signtext_everyone;
    /*  4 */ public static String signtext_timer;
    /*  5 */ public static String cmd_help_editsign = "/deadbolt <line number> <text> - Edit signs on locked containers, right click a sign first to select it";
    /*  6 */ public static String cmd_help_reload = "/deadbolt reload - Reload the config.yml and <language>.yml files";
    /*  7 */ public static String cmd_reload = "Reloading settings...";
    /*  8 */ public static String cmd_sign_updated = "Sign updated";
    /*  9 */ public static String cmd_sign_selected = "Sign selected, use /deadbolt <line number> <text>";
    /* 10 */ public static String cmd_sign_selected_error = "Selected sign has an error. Right click it again";
    /* 11 */ public static String cmd_sign_not_selected = "Nothing selected, right click a valid sign first";
    /* 12 */ public static String cmd_identifier_not_changeable = "The identifier on line 1 is not changeable, except for color.";
    /* 13 */ public static String cmd_owner_not_changeable = "The owner on line 2 is not changeable, except for color.";
    /* 14 */ public static String cmd_line_num_out_of_range = "Bad format, your line number should be 1,2,3,4";
    /* 15 */ public static String cmd_command_not_found = "No command found, use \"/deadbolt\" for options";
    /* 16 */ public static String cmd_console_reload = "Deadbolt - Reloading settings...";
    /* 17 */ public static String cmd_console_command_not_found = "Deadbolt - No command found, use \"deadbolt\" for options";
    /* 18 */ public static String msg_admin_break = "(Admin) %1$s broke a block owned by %2$s";
    /* 29 */ public static String msg_admin_bypass = "(Admin) Warning, this door is owned by %1$s, make sure to shut it";
    /* 20 */ public static String msg_admin_sign_placed = "(Admin) Warning, this block is owned by %1$s";
    /* 21 */ public static String msg_admin_sign_selection = "(Admin) Warning, selected a sign owned by %1$s";
    /* 22 */ public static String msg_admin_container = "(Admin) %1$s opened a container owned by %2$s";
    /* 23 */ public static String msg_admin_warning_player_not_found = "%1$s is not online, make sure you have the correct name";
    /* 24 */ public static String msg_deny_access_door = "Access denied";
    /* 25 */ public static String msg_deny_access_container = "Access denied";
    /* 26 */ public static String msg_deny_sign_selection = "You don't own this sign";
    /* 27 */ public static String msg_deny_block_break = "You don't own this block";
    /* 28 */ public static String msg_deny_container_expansion = "You don't own the adjacent container";
    /* 29 */ public static String msg_deny_door_expansion = "You don't own the adjacent door";
    /* 30 */ public static String msg_deny_trapdoor_placement = "You don't own the adjacent hinge block";
    /* 31 */ public static String msg_deny_fencegate_placement = "You don't own the adjacent fence gate";
    /* 32 */ public static String msg_deny_sign_private_nothing_nearby = "Nothing nearby to protect";
    /* 33 */ public static String msg_deny_sign_private_already_owned = "This block is already protected";
    /* 34 */ public static String msg_deny_sign_moreusers_already_owned = "You don't own this block";
    /* 35 */ public static String msg_deny_sign_moreusers_no_private = "No sign with [Private] nearby";
    /* 36 */ public static String msg_deny_sign_quickplace = "You cant protect this block, %1$s already has";
    /* 37 */ public static String msg_deny_block_perm = "You are not authorized to protect %1$s";
    /* 38 */ public static String msg_tnt_fizzle = "TNT tried to explode too close to a protected block";
    /* 39 */ public static String msg_reminder_lock_your_chests = "Place a sign headed [Private] next to your chest to lock it";
    //------------------------------------------------------------------------//

    public Conf(final Deadbolt plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        File configFile = new File(plugin.getDataFolder() + File.separator + "config.yml");
        if (!configFile.exists())
            downloadFile("config.yml");
        Configuration config = new Configuration(configFile);
        config.load();

        /*  3 */ groupContainers = config.getBoolean("group-containers", groupContainers);
        /*  5 */ pistonProtection = config.getBoolean("piston-protection", pistonProtection);
        /*  7 */ redstoneProtection = config.getBoolean("redstone-protection", redstoneProtection);
        /*  9 */ explosionProtection = config.getBoolean("explosion-protection", explosionProtection);
        /* 10 */ broadcastTNT = config.getBoolean("broadcast-tnt-fizzle", broadcastTNT);
        /* 11 */ broadcastTNTRadius = config.getInt("broadcast-tnt-fizzle-radius", broadcastTNTRadius);
        /* 20 */ default_color_private[0] = "§" + config.getString("default_color_private_line_1", default_color_private[0]);
        /* 21 */ default_color_private[1] = "§" + config.getString("default_color_private_line_2", default_color_private[1]);
        /* 22 */ default_color_private[2] = "§" + config.getString("default_color_private_line_3", default_color_private[2]);
        /* 23 */ default_color_private[3] = "§" + config.getString("default_color_private_line_4", default_color_private[3]);
        /* 24 */ default_color_moreusers[0] = "§" + config.getString("default_color_moreusers_line_1", default_color_moreusers[0]);
        /* 25 */ default_color_moreusers[1] = "§" + config.getString("default_color_moreusers_line_2", default_color_moreusers[1]);
        /* 26 */ default_color_moreusers[2] = "§" + config.getString("default_color_moreusers_line_3", default_color_moreusers[2]);
        /* 27 */ default_color_moreusers[3] = "§" + config.getString("default_color_moreusers_line_4", default_color_moreusers[3]);
        for (int i = 0; i < 4; i++) {
            if (!COLORPATTERN.matcher(default_color_private[i]).matches()) {
                default_color_private[i] = "§0";
                Bukkit.getLogger().log(Level.WARNING, String.format("Deadbolt: Default private color " + (i + 1) + " is not within [0-9a-fA-F]"));
            }
            if (!COLORPATTERN.matcher(default_color_moreusers[i]).matches()) {
                default_color_moreusers[i] = "§0";
                Bukkit.getLogger().log(Level.WARNING, String.format("Deadbolt: Default moreusers color " + (i + 1) + " is not within [0-9a-fA-F]"));
            }
        }
        
        /* 29 */ defaultTimer = config.getInt("default_door_timer", defaultTimer);
        /* 31 */ deselectSign = config.getBoolean("clear-sign-selection", deselectSign);
        /* 32 */ silentDoorSounds = config.getBoolean("silent-door-sounds", silentDoorSounds);
        /* 34 */ timedDoorSounds = config.getBoolean("timed-door-sounds", timedDoorSounds);


        /*  1 */ String language = config.getString("language", "english.yml");
        File langfile = new File(plugin.getDataFolder() + File.separator + language);
        if (!langfile.exists())
            downloadFile(language);
        loadMessages(langfile);
    }

    private void loadMessages(File langfile) {
        Configuration locale = new Configuration(langfile);
        locale.load();

        /*  1 */ signtext_private = locale.getString("signtext_private", DEFAULT_SIGNTEXT_PRIVATE);
        signtext_private = signtext_private.length() > 13 ? signtext_private.substring(0, 14) : signtext_private;
        pattern_signtext_private = Pattern.compile("\\[(?i)(" + DEFAULT_SIGNTEXT_PRIVATE + "|" + signtext_private + ")\\]");

        /*  2 */ signtext_moreusers = locale.getString("signtext_moreusers", DEFAULT_SIGNTEXT_MOREUSERS);
        signtext_moreusers = (signtext_moreusers.length() > 13 ? signtext_moreusers.substring(0, 14) : signtext_moreusers);
        pattern_signtext_moreusers = Pattern.compile("\\[(?i)(" + DEFAULT_SIGNTEXT_MOREUSERS + "|" + signtext_moreusers + ")\\]");

        /*  3 */ signtext_everyone = locale.getString("signtext_everyone", DEFAULT_SIGNTEXT_EVERYONE);
        signtext_everyone = (signtext_everyone.length() > 13 ? signtext_everyone.substring(0, 14) : signtext_everyone);
        pattern_signtext_everyone = Pattern.compile("\\[(?i)(" + DEFAULT_SIGNTEXT_EVERYONE + "|" + signtext_everyone + ")\\]");

        /*  4 */ signtext_timer = locale.getString("signtext_timer", DEFAULT_SIGNTEXT_TIMER);
        signtext_timer = (signtext_timer.length() > 10 ? signtext_timer.substring(0, 11) : signtext_timer);
        pattern_signtext_timer = Pattern.compile("\\[(?i)(" + DEFAULT_SIGNTEXT_TIMER + "|" + signtext_timer + "):[0-9]\\]");
        
    /*  5 */ cmd_help_editsign = locale.getString("cmd_help_editsign",cmd_help_editsign); 
    /*  6 */ cmd_help_reload = locale.getString("cmd_help_reload",cmd_help_reload); 
    /*  7 */ cmd_reload = locale.getString("cmd_reload",cmd_reload); 
    /*  8 */ cmd_sign_updated = locale.getString("cmd_sign_updated",cmd_sign_updated); 
    /*  9 */ cmd_sign_selected = locale.getString("cmd_sign_selected",cmd_sign_selected); 
    /* 10 */ cmd_sign_selected_error = locale.getString("cmd_sign_selected_error",cmd_sign_selected_error); 
    /* 11 */ cmd_sign_not_selected = locale.getString("cmd_sign_not_selected",cmd_sign_not_selected); 
    /* 12 */ cmd_identifier_not_changeable = locale.getString("cmd_identifier_not_changeable",cmd_identifier_not_changeable);
    /* 13 */ cmd_owner_not_changeable = locale.getString("cmd_owner_not_changeable",cmd_owner_not_changeable); 
    /* 14 */ cmd_line_num_out_of_range = locale.getString("cmd_line_num_out_of_range",cmd_line_num_out_of_range);
    /* 15 */ cmd_command_not_found = locale.getString("cmd_command_not_found",cmd_command_not_found);
    /* 16 */ cmd_console_reload = locale.getString("cmd_console_reload",cmd_console_reload); 
    /* 17 */ cmd_console_command_not_found = locale.getString("cmd_console_command_not_found",cmd_console_command_not_found);
    /* 18 */ msg_admin_break = locale.getString("msg_admin_break",msg_admin_break);
    /* 29 */ msg_admin_bypass = locale.getString("msg_admin_bypass",msg_admin_bypass);
    /* 20 */ msg_admin_sign_placed = locale.getString("msg_admin_sign_placed",msg_admin_sign_placed); 
    /* 21 */ msg_admin_sign_selection = locale.getString("msg_admin_sign_selection",msg_admin_sign_selection);
    /* 22 */ msg_admin_container = locale.getString("msg_admin_container",msg_admin_container); 
    /* 23 */ msg_admin_warning_player_not_found = locale.getString("msg_admin_warning_player_not_found",msg_admin_warning_player_not_found);
    /* 24 */ msg_deny_access_door = locale.getString("msg_deny_access_door",msg_deny_access_door); 
    /* 25 */ msg_deny_access_container = locale.getString("msg_deny_access_container",msg_deny_access_container); 
    /* 26 */ msg_deny_sign_selection = locale.getString("msg_deny_sign_selection",msg_deny_sign_selection);
    /* 27 */ msg_deny_block_break = locale.getString("msg_deny_block_break",msg_deny_block_break);
    /* 28 */ msg_deny_container_expansion = locale.getString("msg_deny_container_expansion",msg_deny_container_expansion); 
    /* 29 */ msg_deny_door_expansion = locale.getString("msg_deny_door_expansion",msg_deny_door_expansion);
    /* 30 */ msg_deny_trapdoor_placement = locale.getString("msg_deny_trapdoor_placement",msg_deny_trapdoor_placement);
    /* 31 */ msg_deny_fencegate_placement = locale.getString("msg_deny_fencegate_placement",msg_deny_fencegate_placement); 
    /* 32 */ msg_deny_sign_private_nothing_nearby = locale.getString("msg_deny_sign_private_nothing_nearby",msg_deny_sign_private_nothing_nearby); 
    /* 33 */ msg_deny_sign_private_already_owned = locale.getString("msg_deny_sign_private_already_owned",msg_deny_sign_private_already_owned);
    /* 34 */ msg_deny_sign_moreusers_already_owned = locale.getString("msg_deny_sign_moreusers_already_owned",msg_deny_sign_moreusers_already_owned);
    /* 35 */ msg_deny_sign_moreusers_no_private = locale.getString("msg_deny_sign_moreusers_no_private",msg_deny_sign_moreusers_no_private); 
    /* 36 */ msg_deny_sign_quickplace = locale.getString("msg_deny_sign_quickplace",msg_deny_sign_quickplace);
    /* 37 */ msg_deny_block_perm = locale.getString("msg_deny_block_perm",msg_deny_block_perm);
    /* 38 */ msg_tnt_fizzle = locale.getString("msg_tnt_fizzle",msg_tnt_fizzle);
    /* 39 */ msg_reminder_lock_your_chests = locale.getString("msg_reminder_lock_your_chests",msg_reminder_lock_your_chests);
    }

    private void downloadFile(String filename) {
        //Southpaw018 - Cenotaph
        if (!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdirs();
        String datafile = plugin.getDataFolder().getPath() + File.separator + filename;
        String repofile = REPO + filename;
        File download = new File(datafile);
        try {
            download.createNewFile();
            URL link = new URL(repofile);
            ReadableByteChannel rbc = Channels.newChannel(link.openStream());
            FileOutputStream fos = new FileOutputStream(download);
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            Bukkit.getLogger().log(Level.INFO, "Deadbolt: Downloaded file ".concat(datafile));
        } catch (MalformedURLException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Deadbolt: Malformed URL ".concat(repofile));
            download.delete();
        } catch (FileNotFoundException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Deadbolt: File not found ".concat(datafile));
            download.delete();
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Deadbolt: IOError downloading ".concat(repofile));
            download.delete();
        }
    }

    public static String stripColor(String text) {
        return text == null ? null : COLORPATTERN.matcher(text).replaceAll("");
    }

    public static String replaceColor(String text) {
        return text == null ? null : PRECOLORPATTERN.matcher(text).replaceAll("§$2");
    }

    public static boolean isPrivate(String ident) {
        return ident == null ? false : pattern_signtext_private.matcher(stripColor(ident)).matches();
    }

    public static boolean isMoreUsers(String ident) {
        return ident == null ? false : pattern_signtext_moreusers.matcher(stripColor(ident)).matches();
    }

    public static boolean isEveryone(String text) {
        return text == null ? false : pattern_signtext_everyone.matcher(stripColor(text)).matches();
    }

    public static boolean isTimer(String text) {
        return text == null ? false : pattern_signtext_timer.matcher(stripColor(text)).matches();
    }

    public static String truncate(String text, int max) {
        return text == null ? null : text.length() > max ? text.substring(0, max) : text;
    }

    public static void sendMessage(Player player, String msg, ChatColor color) {
        if (msg.equals(""))
            return;
        player.sendMessage(color + TAG + msg);
    }

    public static void sendBroadcast(String perm, String msg, ChatColor color) {
        if (!msg.isEmpty())
            for (Player player : Bukkit.getServer().getOnlinePlayers())
                if (player.hasPermission(perm))
                    player.sendMessage(color + TAG + msg);
    }

    public static String getLine(Sign sign, int line) {
        return stripColor(sign.getLine(line));
    }

    public static void setLines(Sign sign, String lines[]) {
        for (int i = 0; i < 4; i++)
            setLine(sign, i, lines[i]);
        sign.update(true);
    }

    public static void setLine(Sign sign, int num, String line) {
        while (line.startsWith("§0"))
            line = line.substring(2);
        line = TWOCOLORPATTERN.matcher(line).replaceAll("$2$3");
        line = truncate(line, 15);
        sign.setLine(num, line);
    }

    public static int countColors(String text) {
        Matcher match = COLORPATTERN.matcher(text);
        int i = 0;
        while (match.find())
            i++;
        return i;
    }
}
