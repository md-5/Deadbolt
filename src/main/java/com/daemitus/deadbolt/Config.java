package com.daemitus.deadbolt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public final class Config {

    //------------------------------------------------------------------------//
    private final Deadbolt plugin;
    private static final String TAG = "Deadbolt: ";
    //------------------------------------------------------------------------//
    public static boolean useOPlist = true;
    public static boolean deselectSign = false;
    public static boolean deny_explosions = true;
    public static boolean deny_endermen = true;
    public static boolean deny_redstone = true;
    public static boolean deny_pistons = true;
    public static boolean deny_quick_signs = false;
    public static List<Integer> redstone_protected_blockids = Arrays.asList(64, 71, 96);
    public static Set<Player> reminder = new HashSet<Player>();
    public static Map<Player, Block> selectedSign = new HashMap<Player, Block>();
    public static boolean vertical_trapdoors = true;
    public static boolean group_furnaces = true;
    public static boolean group_dispensers = true;
    public static boolean silent_door_sounds = true;
    public static boolean deny_timed_doors = false;
    public static boolean timed_door_sounds = true;
    public static boolean forced_timed_doors = false;
    public static int forced_timed_doors_delay = 3;
    //------------------------------------------------------------------------//
    public static String locale_private;
    public static String locale_moreusers;
    public static Pattern signtext_private;
    public static Pattern signtext_moreusers;
    public static Pattern signtext_everyone;
    public static Pattern signtext_timer;
    //------------------------------------------------------------------------//
    public static final Set<BlockFace> CARDINAL_FACES = EnumSet.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);
    public static final Set<BlockFace> VERTICAL_FACES = EnumSet.of(BlockFace.UP, BlockFace.DOWN);
    //------------------------------------------------------------------------//
    public static String cmd_help_editsign = "/deadbolt <line number> <text> - Edit signs on locked containers, right click a sign first to select it";
    public static String cmd_help_reload = "/deadbolt reload - Reload the config.yml and <language>.yml files";
    public static String cmd_help_fix = "/deadbolt fix - toggle a single block";
    public static String cmd_help_fixAll = "/deadbolt fixall - toggle all related blocks";
    public static String cmd_reload = "Reloading settings...";
    public static String cmd_fix_notowned = "You don't own that block";
    public static String cmd_fix_bad_type = "You can only fix blocks that open and close";
    public static String cmd_sign_updated = "Sign updated";
    public static String cmd_sign_selected = "Sign selected, use /deadbolt <line number> <text>";
    public static String cmd_sign_selected_error = "Selected sign has an error. Right click it again";
    public static String cmd_sign_not_selected = "Nothing selected, right click a valid sign first";
    public static String cmd_identifier_not_changeable = "The identifier on line 1 is not changeable, except for color.";
    public static String cmd_owner_not_changeable = "The owner on line 2 is not changeable, except for color.";
    public static String cmd_line_num_out_of_range = "Bad format, your line number should be 1,2,3,4";
    public static String cmd_command_not_found = "No command found, use \"/deadbolt\" for options";
    public static String cmd_console_reload = "Deadbolt - Reloading settings...";
    public static String cmd_console_command_not_found = "Deadbolt - No command found, use \"deadbolt\" for options";
    public static String msg_admin_break = "(Admin) %1$s broke a block owned by %2$s";
    public static String msg_admin_bypass = "(Admin) Warning, this door is owned by %1$s, make sure to shut it";
    public static String msg_admin_sign_placed = "(Admin) Warning, this block is owned by %1$s";
    public static String msg_admin_sign_selection = "(Admin) Warning, selected a sign owned by %1$s";
    public static String msg_admin_block_fixed = "(Admin) Warning, fixed a block owned by %1$s";
    public static String msg_admin_container = "(Admin) %1$s opened a container owned by %2$s";
    public static String msg_admin_warning_player_not_found = "%1$s is not online, make sure you have the correct name";
    public static String msg_deny_access_door = "Access denied";
    public static String msg_deny_access_container = "Access denied";
    public static String msg_deny_sign_selection = "You don't own this sign";
    public static String msg_deny_block_break = "You don't own this block";
    public static String msg_deny_container_expansion = "You don't own the adjacent container";
    public static String msg_deny_door_expansion = "You don't own the adjacent door";
    public static String msg_deny_trapdoor_expansion = "You don't own the adjacent trapdoor/hinge block";
    public static String msg_deny_fencegate_expansion = "You don't own the adjacent fencegate/nearby block";
    public static String msg_deny_sign_private_nothing_nearby = "Nothing nearby to protect";
    public static String msg_deny_sign_private_already_owned = "This block is already protected";
    public static String msg_deny_sign_moreusers_already_owned = "You don't own this block";
    public static String msg_deny_sign_moreusers_no_private = "No sign with [Private] nearby";
    public static String msg_deny_sign_quickplace = "You cant protect this block, %1$s already has";
    public static String msg_deny_block_perm = "You are not authorized to protect %1$s";
    public static String msg_reminder_lock_your_chests = "Place a sign headed [Private] next to your block to lock it";
    //------------------------------------------------------------------------//
    private static final Pattern DETECT_COLORS = Pattern.compile("§([0-9a-f])");
    private static final Pattern TWO_COLORS = Pattern.compile("(§[0-9a-f])(\\s*)(§[0-9a-f])");
    private static final Pattern PSEUDO_COLOR = Pattern.compile("\\&([0-9a-f])");
    private static final Pattern UNNEEDED_COLOR = Pattern.compile("^§0");
    private static final Pattern FORMAT_LENGTH = Pattern.compile("(^.{0,15}).*");
    public static String[] default_colors_private = {"0", "0", "0", "0"};
    public static String[] default_colors_moreusers = {"0", "0", "0", "0"};
    //------------------------------------------------------------------------//    

    public Config(final Deadbolt plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        try {
            YamlConfiguration config = new YamlConfiguration();

            File configFile = new File(plugin.getDataFolder() + "/config.yml");
            checkFile(plugin, configFile);
            config.load(configFile);

            useOPlist = config.getBoolean("useOPlist", useOPlist);
            vertical_trapdoors = config.getBoolean("vertical_trapdoors", vertical_trapdoors);
            group_furnaces = config.getBoolean("group_furnaces", group_furnaces);
            group_dispensers = config.getBoolean("group_dispensers", group_dispensers);
            deny_quick_signs = config.getBoolean("deny_quick_signs", deny_quick_signs);
            deselectSign = config.getBoolean("clear_sign_selection", deselectSign);
            deny_explosions = config.getBoolean("deny_explosions", deny_explosions);
            deny_endermen = config.getBoolean("deny_endermen", deny_endermen);
            deny_pistons = config.getBoolean("deny_pistons", deny_pistons);
            deny_redstone = config.getBoolean("deny_redstone", deny_redstone);
            redstone_protected_blockids = config.getList("deny_redstone_specific_ids", redstone_protected_blockids);
            silent_door_sounds = config.getBoolean("silent_door_sounds", silent_door_sounds);
            deny_timed_doors = config.getBoolean("deny_timed_doors", deny_timed_doors);
            timed_door_sounds = config.getBoolean("timed_door_sounds", timed_door_sounds);
            forced_timed_doors = config.getBoolean("forced_timed_doors", forced_timed_doors);
            forced_timed_doors_delay = config.getInt("forced_timed_doors_delay", forced_timed_doors_delay);

            default_colors_private[0] = "§" + config.getString("default_color_private_line_1", default_colors_private[0]);
            default_colors_private[1] = "§" + config.getString("default_color_private_line_2", default_colors_private[1]);
            default_colors_private[2] = "§" + config.getString("default_color_private_line_3", default_colors_private[2]);
            default_colors_private[3] = "§" + config.getString("default_color_private_line_4", default_colors_private[3]);
            default_colors_moreusers[0] = "§" + config.getString("default_color_moreusers_line_1", default_colors_moreusers[0]);
            default_colors_moreusers[1] = "§" + config.getString("default_color_moreusers_line_2", default_colors_moreusers[1]);
            default_colors_moreusers[2] = "§" + config.getString("default_color_moreusers_line_3", default_colors_moreusers[2]);
            default_colors_moreusers[3] = "§" + config.getString("default_color_moreusers_line_4", default_colors_moreusers[3]);

            String language = config.getString("language", "english.yml");

            File langFile = new File(plugin.getDataFolder() + "/" + language);
            if (!checkFile(plugin, langFile)) {
                Deadbolt.logger.log(Level.WARNING, "[Deadbolt] " + langFile.getName() + " not found, defaulting to english.yml");
                checkFile(plugin, langFile = new File(plugin.getDataFolder() + "/english.yml"));
            }
            config.load(langFile);

            String default_private = "private";
            locale_private = config.getString("signtext_private", default_private);
            if (locale_private.length() > 13)
                Deadbolt.logger.log(Level.WARNING, "[Deadbolt] " + locale_private + " is too long, defaulting to [" + (locale_private = default_private) + "]");
            signtext_private = Pattern.compile("\\[(?i)(" + default_private + "|" + locale_private + ")\\]");
            locale_private = "[" + locale_private + "]";

            String default_moreusers = "more users";
            locale_moreusers = config.getString("signtext_moreusers", default_moreusers);
            if (locale_moreusers.length() > 13)
                Deadbolt.logger.log(Level.WARNING, "[Deadbolt] " + locale_moreusers + " is too long, defaulting to [" + (locale_private = default_moreusers) + "]");
            signtext_moreusers = Pattern.compile("\\[(?i)(" + default_moreusers + "|" + locale_moreusers + ")\\]");
            locale_moreusers = "[" + locale_moreusers + "]";

            String default_everyone = "everyone";
            String locale_everyone = config.getString("signtext_everyone", default_everyone);
            if (locale_everyone.length() > 13)
                Deadbolt.logger.log(Level.WARNING, "[Deadbolt] " + locale_everyone + " is too long, defaulting to [" + (locale_private = default_everyone) + "]");
            signtext_everyone = Pattern.compile("\\[(?i)(" + default_everyone + "|" + locale_everyone + ")\\]");

            String default_timer = "timer";
            String locale_timer = config.getString("signtext_timer", default_timer);
            if (locale_timer.length() > 13)
                Deadbolt.logger.log(Level.WARNING, "[Deadbolt] " + locale_timer + " is too long, defaulting to [" + (locale_private = default_timer) + ":#]");
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
        } catch (FileNotFoundException ex) {
            Deadbolt.logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Deadbolt.logger.log(Level.SEVERE, null, ex);
        } catch (InvalidConfigurationException ex) {
            Deadbolt.logger.log(Level.SEVERE, null, ex);
        }
    }

    private static boolean checkFile(final Deadbolt plugin, File file) {
        try {
            if (file.exists())
                return true;

            InputStream fis = plugin.getResource("files/" + file.getName());
            if (fis == null)
                return false;
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);

            try {
                byte[] buf = new byte[1024];
                int i = 0;
                while ((i = fis.read(buf)) != -1) {
                    fos.write(buf, 0, i);
                }
            } catch (Exception e) {
                e.printStackTrace(System.out);
            } finally {
                if (fis != null) {
                    fis.close();
                }
                if (fos != null) {
                    fos.close();
                }
            }
            Deadbolt.logger.log(Level.INFO, String.format("Deadbolt: Retrieved file %1$s", file.getName()));
            return true;
        } catch (IOException ex) {
            Deadbolt.logger.log(Level.SEVERE, String.format("Deadbolt: Error retrieving %1$s", file.getName()));
            return false;
        }
    }

    public static String formatForSign(String line) {
        line = UNNEEDED_COLOR.matcher(line).replaceAll("");
        line = TWO_COLORS.matcher(line).replaceAll("$2$3");
        line = FORMAT_LENGTH.matcher(line).replaceAll("$1");
        line = line.substring(0, line.length() > 15 ? 15 : line.length());
        return line;
    }

    public static String removeColor(String text) {
        return text == null ? null : DETECT_COLORS.matcher(text).replaceAll("");
    }

    public static String createColor(String text) {
        return text == null ? null : PSEUDO_COLOR.matcher(text).replaceAll("§$1");
    }

    public static String getLine(Sign signBlock, int line) {
        return DETECT_COLORS.matcher(signBlock.getLine(line)).replaceAll("");
    }

    public static boolean isPrivate(String line) {
        return signtext_private.matcher(line).matches();
    }

    public static boolean isMoreUsers(String line) {
        return signtext_moreusers.matcher(line).matches();
    }

    public static boolean isEveryone(String line) {
        return signtext_everyone.matcher(line).matches();
    }

    public static boolean isTimer(String line) {
        return signtext_timer.matcher(line).matches();
    }

    public static int getTimer(String line) {
        try {
            return Integer.parseInt(signtext_timer.matcher(line).replaceAll("$1%d"));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    public static boolean isValidWallSign(Sign signState) {
        String line = getLine(signState, 0);
        return isPrivate(line) || isMoreUsers(line);
    }

    public static Block getSignAttached(Sign signState) {
        return signState.getBlock().getRelative(((org.bukkit.material.Sign) signState.getData()).getAttachedFace());
    }

    public static void sendMessage(Player player, ChatColor color, String message, String... args) {
        if (!message.isEmpty())
            player.sendMessage(color + TAG + String.format(message, (Object[]) args));
    }

    public static void sendBroadcast(String permission, ChatColor color, String message, String... args) {
        if (!message.isEmpty())
            for (Player player : Bukkit.getServer().getOnlinePlayers())
                if (player.hasPermission(permission))
                    player.sendMessage(color + TAG + String.format(message, (Object[]) args));
    }

    public static boolean hasPermission(Player player, String permission) {
        return (Config.useOPlist ? player.isOp() : false) || player.hasPermission(permission);
    }

    public static String truncateName(String name) {
        return name.substring(0, name.length() > 13 ? 13 : name.length());
    }

    public static BlockFace getFacingFromByte(byte b) {
        switch (b) {
            case 0x2:
                return BlockFace.EAST;
            case 0x3:
                return BlockFace.WEST;
            case 0x4:
                return BlockFace.NORTH;
            case 0x5:
                return BlockFace.SOUTH;
            default:
                return null;
        }
    }

    public static byte getByteFromFacing(BlockFace bf) {
        switch (bf) {
            case EAST:
                return 0x2;
            case WEST:
                return 0x3;
            case NORTH:
                return 0x4;
            case SOUTH:
                return 0x5;
            default:
                return 0x0;
        }

    }
}