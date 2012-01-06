package com.daemitus.deadbolt;

import com.daemitus.deadbolt.util.Util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Config {

    //------------------------------------------------------------------------//
    private static final Deadbolt plugin = Deadbolt.instance;
    private final String TAG = "Deadbolt: ";
    //------------------------------------------------------------------------//
    public boolean useOPlist = true;
    public boolean deselectSign = false;
    public boolean deny_entity_interact = true;
    public boolean deny_explosions = true;
    public boolean deny_endermen = true;
    public boolean deny_redstone = true;
    public boolean deny_pistons = true;
    public boolean deny_quick_signs = false;
    public List<Integer> redstone_protected_blockids = Arrays.asList(64, 71, 96);
    public Set<Player> reminder = new HashSet<Player>();
    public Map<Player, Block> selectedSign = new HashMap<Player, Block>();
    public boolean vertical_trapdoors = true;
    public boolean group_furnaces = true;
    public boolean group_dispensers = true;
    public boolean group_cauldrons = true;
    public boolean group_enchantment_tables = true;
    public boolean group_brewing_stands = true;
    public boolean silent_door_sounds = true;
    public boolean deny_timed_doors = false;
    public boolean timed_door_sounds = true;
    public boolean forced_timed_doors = false;
    public int forced_timed_doors_delay = 3;
    //------------------------------------------------------------------------//
    public String locale_private;
    public String locale_moreusers;
    public Pattern signtext_private;
    public Pattern signtext_moreusers;
    public Pattern signtext_everyone;
    public Pattern signtext_timer;
    //------------------------------------------------------------------------//
    public final Set<BlockFace> CARDINAL_FACES = EnumSet.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);
    public final Set<BlockFace> VERTICAL_FACES = EnumSet.of(BlockFace.UP, BlockFace.DOWN);
    //------------------------------------------------------------------------//
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
    public String msg_deny_block_perm = "You are not authorized to protect %1$s";
    public String msg_reminder_lock_your_chests = "Place a sign headed [Private] next to your block to lock it";
    //------------------------------------------------------------------------//
    public String[] default_colors_private = {"", "", "", ""};
    public String[] default_colors_moreusers = {"", "", "", ""};
    //------------------------------------------------------------------------//    

    public void load() {
        File configFile = new File(plugin.getDataFolder() + "/config.yml");
        checkFile(configFile);

        FileConfiguration config = plugin.getConfig();

        useOPlist = config.getBoolean("useOPlist", useOPlist);
        vertical_trapdoors = config.getBoolean("vertical_trapdoors", vertical_trapdoors);
        group_furnaces = config.getBoolean("group_furnaces", group_furnaces);
        group_dispensers = config.getBoolean("group_dispensers", group_dispensers);
        group_cauldrons = config.getBoolean("group_cauldrons", group_cauldrons);
        group_enchantment_tables = config.getBoolean("group_enchantment_tables", group_enchantment_tables);
        group_brewing_stands = config.getBoolean("group_brewing_stands", group_brewing_stands);
        deny_quick_signs = config.getBoolean("deny_quick_signs", deny_quick_signs);
        deselectSign = config.getBoolean("clear_sign_selection", deselectSign);
        deny_explosions = config.getBoolean("deny_explosions", deny_explosions);
        deny_endermen = config.getBoolean("deny_endermen", deny_endermen);
        deny_pistons = config.getBoolean("deny_pistons", deny_pistons);
        deny_redstone = config.getBoolean("deny_redstone", deny_redstone);
        redstone_protected_blockids = config.getIntegerList("deny_redstone_specific_ids"); //Why doesnt this have a default param
        silent_door_sounds = config.getBoolean("silent_door_sounds", silent_door_sounds);
        deny_timed_doors = config.getBoolean("deny_timed_doors", deny_timed_doors);
        timed_door_sounds = config.getBoolean("timed_door_sounds", timed_door_sounds);
        forced_timed_doors = config.getBoolean("forced_timed_doors", forced_timed_doors);
        forced_timed_doors_delay = config.getInt("forced_timed_doors_delay", forced_timed_doors_delay);

        default_colors_private[0] = "§" + config.getString("default_colors_private_line_1", "0");
        default_colors_private[1] = "§" + config.getString("default_colors_private_line_2", "0");
        default_colors_private[2] = "§" + config.getString("default_colors_private_line_3", "0");
        default_colors_private[3] = "§" + config.getString("default_colors_private_line_4", "0");
        default_colors_moreusers[0] = "§" + config.getString("default_colors_moreusers_line_1", "0");
        default_colors_moreusers[1] = "§" + config.getString("default_colors_moreusers_line_2", "0");
        default_colors_moreusers[2] = "§" + config.getString("default_colors_moreusers_line_3", "0");
        default_colors_moreusers[3] = "§" + config.getString("default_colors_moreusers_line_4", "0");

        String language = config.getString("language", "english.yml");

        File langFile = new File(plugin.getDataFolder() + "/" + language);
        if (!checkFile(langFile)) {
            Deadbolt.logger.log(Level.WARNING, "[Deadbolt] " + langFile.getName() + " not found, defaulting to english.yml");
            checkFile(langFile = new File(plugin.getDataFolder() + "/english.yml"));
        }

        config = YamlConfiguration.loadConfiguration(langFile);

        String default_private = "private";
        locale_private = config.getString("signtext_private", default_private);
        if (locale_private.length() > 13) {
            Deadbolt.logger.log(Level.WARNING, "[Deadbolt] " + locale_private + " is too long, defaulting to [" + (locale_private = default_private) + "]");
        }
        signtext_private = Pattern.compile("\\[(?i)(" + default_private + "|" + locale_private + ")\\]");
        locale_private = "[" + locale_private + "]";

        String default_moreusers = "more users";
        locale_moreusers = config.getString("signtext_moreusers", default_moreusers);
        if (locale_moreusers.length() > 13) {
            Deadbolt.logger.log(Level.WARNING, "[Deadbolt] " + locale_moreusers + " is too long, defaulting to [" + (locale_private = default_moreusers) + "]");
        }
        signtext_moreusers = Pattern.compile("\\[(?i)(" + default_moreusers + "|" + locale_moreusers + ")\\]");
        locale_moreusers = "[" + locale_moreusers + "]";

        String default_everyone = "everyone";
        String locale_everyone = config.getString("signtext_everyone", default_everyone);
        if (locale_everyone.length() > 13) {
            Deadbolt.logger.log(Level.WARNING, "[Deadbolt] " + locale_everyone + " is too long, defaulting to [" + (locale_private = default_everyone) + "]");
        }
        signtext_everyone = Pattern.compile("\\[(?i)(" + default_everyone + "|" + locale_everyone + ")\\]");

        String default_timer = "timer";
        String locale_timer = config.getString("signtext_timer", default_timer);
        if (locale_timer.length() > 13) {
            Deadbolt.logger.log(Level.WARNING, "[Deadbolt] " + locale_timer + " is too long, defaulting to [" + (locale_private = default_timer) + ":#]");
        }
        signtext_timer = Pattern.compile("\\[(?i)(" + default_timer + "|" + locale_timer + "):\\s*([0-9]+)\\]");


        cmd_help_editsign = config.getString("cmd_help_editsign", cmd_help_editsign);
        cmd_help_reload = config.getString("cmd_help_reload", cmd_help_reload);
        cmd_help_fix = config.getString("cmd_help_fix", cmd_help_fix);
        cmd_help_fixAll = config.getString("cmd_help_fixAll", cmd_help_fixAll);
        cmd_fix_notowned = config.getString("cmd_fix_notowned", cmd_fix_notowned);
        cmd_fix_bad_type = config.getString("cmd_fix_bad_type", cmd_fix_bad_type);
        cmd_reload = config.getString("cmd_reload", cmd_reload);
        cmd_sign_updated = config.getString("cmd_sign_updated", cmd_sign_updated);
        cmd_sign_selected = config.getString("cmd_sign_selected", cmd_sign_selected);
        cmd_sign_selected_error = config.getString("cmd_sign_selected_error", cmd_sign_selected_error);
        cmd_sign_not_selected = config.getString("cmd_sign_not_selected", cmd_sign_not_selected);
        cmd_identifier_not_changeable = config.getString("cmd_identifier_not_changeable", cmd_identifier_not_changeable);
        cmd_owner_not_changeable = config.getString("cmd_owner_not_changeable", cmd_owner_not_changeable);
        cmd_line_num_out_of_range = config.getString("cmd_line_num_out_of_range", cmd_line_num_out_of_range);
        cmd_command_not_found = config.getString("cmd_command_not_found", cmd_command_not_found);
        cmd_console_reload = config.getString("cmd_console_reload", cmd_console_reload);
        cmd_console_command_not_found = config.getString("cmd_console_command_not_found", cmd_console_command_not_found);
        msg_admin_break = config.getString("msg_admin_break", msg_admin_break);
        msg_admin_bypass = config.getString("msg_admin_bypass", msg_admin_bypass);
        msg_admin_sign_placed = config.getString("msg_admin_sign_placed", msg_admin_sign_placed);
        msg_admin_sign_selection = config.getString("msg_admin_sign_selection", msg_admin_sign_selection);
        msg_admin_block_fixed = config.getString("msg_admin_block_fixed", msg_admin_block_fixed);
        msg_admin_container = config.getString("msg_admin_container", msg_admin_container);
        msg_admin_warning_player_not_found = config.getString("msg_admin_warning_player_not_found", msg_admin_warning_player_not_found);
        msg_deny_access_door = config.getString("msg_deny_access_door", msg_deny_access_door);
        msg_deny_access_container = config.getString("msg_deny_access_container", msg_deny_access_container);
        msg_deny_sign_selection = config.getString("msg_deny_sign_selection", msg_deny_sign_selection);
        msg_deny_block_break = config.getString("msg_deny_block_break", msg_deny_block_break);
        msg_deny_container_expansion = config.getString("msg_deny_container_expansion", msg_deny_container_expansion);
        msg_deny_door_expansion = config.getString("msg_deny_door_expansion", msg_deny_door_expansion);
        msg_deny_trapdoor_expansion = config.getString("msg_deny_trapdoor_expansion", msg_deny_trapdoor_expansion);
        msg_deny_fencegate_expansion = config.getString("msg_deny_fencegate_expansion", msg_deny_fencegate_expansion);
        msg_deny_sign_private_nothing_nearby = config.getString("msg_deny_sign_private_nothing_nearby", msg_deny_sign_private_nothing_nearby);
        msg_deny_sign_private_already_owned = config.getString("msg_deny_sign_private_already_owned", msg_deny_sign_private_already_owned);
        msg_deny_sign_moreusers_already_owned = config.getString("msg_deny_sign_moreusers_already_owned", msg_deny_sign_moreusers_already_owned);
        msg_deny_sign_moreusers_no_private = config.getString("msg_deny_sign_moreusers_no_private", msg_deny_sign_moreusers_no_private);
        msg_deny_sign_quickplace = config.getString("msg_deny_sign_quickplace", msg_deny_sign_quickplace);
        msg_deny_block_perm = config.getString("msg_deny_block_perm", msg_deny_block_perm);
        msg_reminder_lock_your_chests = config.getString("msg_reminder_lock_your_chests", msg_reminder_lock_your_chests);
    }

    private static boolean checkFile(File file) {
        try {
            if (file.exists()) {
                return true;
            }

            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdir();
            }

            file.createNewFile();

            InputStream in = null;
            OutputStream out = null;

            try {
                in = Deadbolt.instance.getResource("files/" + file.getName());
                out = new FileOutputStream(file);

                int len;
                byte[] buf = new byte[1024];
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }

            Deadbolt.logger.log(Level.INFO, "[Deadbolt] Retrieved file " + file.getName());
            return true;
        } catch (IOException ex) {
            Deadbolt.logger.log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean isPrivate(String line) {
        return signtext_private.matcher(line).matches();
    }

    public boolean isMoreUsers(String line) {
        return signtext_moreusers.matcher(line).matches();
    }

    public boolean isEveryone(String line) {
        return signtext_everyone.matcher(line).matches();
    }

    public boolean isTimer(String line) {
        return signtext_timer.matcher(line).matches();
    }

    public int getTimer(String line) {
        try {
            return Integer.parseInt(signtext_timer.matcher(line).replaceAll("$2"));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    public boolean isValidWallSign(Sign signState) {
        String line = Util.getLine(signState, 0);
        return isPrivate(line) || isMoreUsers(line);
    }

    public void sendMessage(Player player, ChatColor color, String message, String... args) {
        if (!message.isEmpty()) {
            player.sendMessage(color + TAG + String.format(message, (Object[]) args));
        }
    }

    public void sendBroadcast(String permission, ChatColor color, String message, String... args) {
        if (!message.isEmpty()) {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                if (player.hasPermission(permission)) {
                    player.sendMessage(color + TAG + String.format(message, (Object[]) args));
                }
            }
        }
    }

    // TODO why do we need this?
    public boolean hasPermission(Player player, String permission) {
        return (useOPlist ? player.isOp() : false) || player.hasPermission(permission);
    }
}