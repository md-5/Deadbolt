package com.daemitus.deadbolt;

import com.daemitus.deadbolt.bridge.Bridge;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.material.Door;
import org.bukkit.material.TrapDoor;

public class Util {

    public static final Set<BlockFace> allBlockFaces = EnumSet.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN);
    public static final Set<BlockFace> horizontalBlockFaces = EnumSet.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);
    public static final Set<BlockFace> verticalBlockFaces = EnumSet.of(BlockFace.UP, BlockFace.DOWN);
    public static final Map<Player, Block> selectedSign = new HashMap<>();
    private static final String pluginTag = "Deadbolt: ";
    public static final String patternFindColor = "(\\&)([0-9a-fA-F])";
    public static final String patternReplaceColor = "§$2";
    private static final String patternStripColor = "§[0-9a-fA-F]";
    private static final String patternNormalTooLong = ".{16,}";
    private static final String patternBracketTooLong = "\\[.{14,}\\]";
    private static final String timerPattern = "\\[.{1,11}:[0-9]\\]";
    public static DoorSchedule doorSchedule = new DoorSchedule();

    /**
     * Check if <block> is protected or not
     * @param block The block to be checked
     * @return If <block> is owned
     */
    public static boolean isProtected(Block block) {
        return getOwnerSign(block) != null;
    }

    /**
     * Check if <block> is protected by <name> or not
     * @param name Name to be checked
     * @param block Block to be checked
     * @return If <name> owns <block>
     */
    public static boolean isOwner(String name, Block block) {
        return truncate(name).equalsIgnoreCase(getOwnerName(block));
    }

    /**
     * Check if <name> or [Everyone] is on any of the [Private] or [More Users] signs associated with <block>
     * @param name Name to be checked
     * @param block Block to be checked
     * @return If <name> is authorized to use <block>
     */
    public static boolean isAuthorized(Player player, Block block) {
        List<String> names = getAllNames(block);
        if (names.isEmpty())
            return true;
        else
            return names.contains(truncate(player.getName().toLowerCase()))
                    || names.contains(Config.signtext_everyone)
                    || names.contains(Config.signtext_everyone_locale)
                    || Bridge.isAuthorized(player, names);
    }

    /**
     * Retrieves owner of <block>
     * @param block Block to be checked
     * @return The text on the line below [Private] on the sign associated with <block>. "" if unprotected
     */
    public static String getOwnerName(Block block) {
        Set<Block> set = getAllSigns(block);
        for (Block b : set) {
            if (b.getType().equals(Material.WALL_SIGN)) {
                Sign sign = (Sign) b.getState();
                String text = stripColor(sign.getLine(0));
                if (text.equalsIgnoreCase(Config.signtext_private)
                        || text.equalsIgnoreCase(Config.signtext_private_locale))
                    return stripColor(sign.getLine(1));
            }
        }
        return "";
    }

    /**
     * Retrieves the sign block associated with <block>
     * @param block Block to be checked
     * @return The sign block associated with <block>. Null if unprotected
     */
    public static Block getOwnerSign(Block block) {
        Set<Block> set = getAllSigns(block);
        for (Block b : set) {
            if (b.getType().equals(Material.WALL_SIGN)) {
                Sign sign = (Sign) b.getState();
                String text = stripColor(sign.getLine(0));
                if (text.equalsIgnoreCase(Config.signtext_private)
                        || text.equalsIgnoreCase(Config.signtext_private_locale))
                    return b;
            }
        }
        return null;
    }

    /**
     * Retrieves all names authorized to interact with <block>
     * @param block Block to be checked
     * @return A List<String> containing everything on any [Private] or [More Users] signs associated with <block>
     */
    public static List<String> getAllNames(Block block) {
        Set<Block> signs = getAllSigns(block);
        List<String> names = new ArrayList<>();
        for (Block signBlock : signs) {
            Sign sign = (Sign) signBlock.getState();
            for (int i = 1; i < 4; i++) {
                String line = sign.getLine(i);
                if (!line.isEmpty())
                    names.add(stripColor(line).toLowerCase());
            }
        }
        return names;
    }

    /**
     * Retrieves all sign blocks associated with <block>
     * @param block Block to be checked
     * @return A Set<Block> containing every [Private] or [More Users] sign associated with <block>
     */
    public static Set<Block> getAllSigns(Block block) {
        switch (block.getType()) {
            case WALL_SIGN:
                Sign sign = (Sign) block.getState();
                String text = stripColor(sign.getLine(0));
                if (text.equalsIgnoreCase(Config.signtext_private)
                        || text.equalsIgnoreCase(Config.signtext_moreusers)
                        || text.equalsIgnoreCase(Config.signtext_private_locale)
                        || text.equalsIgnoreCase(Config.signtext_moreusers_locale))
                    return getAllSigns(getBlockSignAttachedTo(block));
                break;
            case CHEST:
                return getAllSigns(block, true, false, false, new HashSet<Block>());
            case DISPENSER:         //descend
            case FURNACE:           //descend
            case BURNING_FURNACE:
                return getAllSigns(block, false, false, false, new HashSet<Block>());
            case WOODEN_DOOR:       //descend
            case IRON_DOOR_BLOCK:
                return getAllSigns(block, true, true, false, new HashSet<Block>());
            case TRAP_DOOR:
                return getAllSigns(block, true, false, true, new HashSet<Block>());
            case FENCE_GATE:
                return getAllSigns(block, true, true, false, new HashSet<Block>());
            case AIR:
                return new HashSet<>();
            default:
                Set<Block> set = new HashSet<>();
                for (BlockFace bf : verticalBlockFaces) {
                    Block vertical = block.getRelative(bf);
                    BlockState state = vertical.getState();
                    if (state.getData() instanceof Door)
                        set.addAll(getAllSigns(vertical));
                }
                for (BlockFace bf : horizontalBlockFaces) {
                    Block horizontal = block.getRelative(bf);
                    BlockState state = horizontal.getState();
                    if (state.getData() instanceof TrapDoor) {
                        TrapDoor door = (TrapDoor) state.getData();
                        if (block.equals(horizontal.getRelative(door.getAttachedFace())))
                            set.addAll(getAllSigns(horizontal));
                    }
                }
                return set;
        }
        return new HashSet<>();
    }

    private static Set<Block> getAllSigns(Block block, boolean iterateHorizontal, boolean iterateVertical, boolean trapDoor, Set<Block> traversed) {
        Set<Block> signSet = new HashSet<>();
        if (!traversed.add(block))
            return signSet;

        for (BlockFace bf : horizontalBlockFaces) {
            Block adjacent = block.getRelative(bf);

            if (iterateHorizontal && adjacent.getType().equals(block.getType()))
                signSet.addAll(getAllSigns(adjacent, true, iterateVertical, trapDoor, traversed));
            if (adjacent.getType().equals(Material.WALL_SIGN)) {
                if ((bf.equals(BlockFace.NORTH) && adjacent.getData() == 4)
                        || (bf.equals(BlockFace.SOUTH) && adjacent.getData() == 5)
                        || (bf.equals(BlockFace.EAST) && adjacent.getData() == 2)
                        || (bf.equals(BlockFace.WEST) && adjacent.getData() == 3)) {
                    Sign sign = (Sign) adjacent.getState();
                    String text = stripColor(sign.getLine(0));
                    if (text.equalsIgnoreCase(Config.signtext_private)
                            || text.equalsIgnoreCase(Config.signtext_private_locale)
                            || text.equalsIgnoreCase(Config.signtext_moreusers)
                            || text.equalsIgnoreCase(Config.signtext_moreusers_locale))
                        signSet.add(adjacent);
                }
            }
        }

        if (iterateVertical) {
            Set<Block> set = new HashSet<>();
            for (BlockFace bf : verticalBlockFaces) {
                Block iterating = block;
                while (iterating.getType().equals(block.getType())) {
                    iterating = iterating.getRelative(bf);
                    set.add(iterating);
                }
            }
            for (Block setBlock : set)
                signSet.addAll(getAllSigns(setBlock, false, false, false, traversed));
        }
        if (trapDoor) {
            TrapDoor door = (TrapDoor) block.getState().getData();
            Block hinge = block.getRelative(door.getAttachedFace());
            signSet.addAll(getAllSigns(hinge, false, false, false, traversed));
        }

        return signSet;
    }

    /**
     * Retrieve the block a given wallsign is attached to
     * @param block The wallsign to be checked
     * @return The block that the wallsign in attached to
     */
    public static Block getBlockSignAttachedTo(Block block) {
        if (block.getType().equals(Material.WALL_SIGN))
            switch (block.getData()) {
                case 2:
                    return block.getRelative(BlockFace.WEST);
                case 3:
                    return block.getRelative(BlockFace.EAST);
                case 4:
                    return block.getRelative(BlockFace.SOUTH);
                case 5:
                    return block.getRelative(BlockFace.NORTH);
            }
        return null;
    }

    /**
     * Removes any color codes from <text>, i.e. §4, §e using "(?i)§[0-9a-zA-Z]"
     * @param text String to be parsed
     * @return A String without any color codes present
     */
    public static String stripColor(String text) {
        return text.replaceAll(patternStripColor, "");
    }

    /**
     * Verifies that <text> is 15 or less characters to fit a sign. 13 characters if <text> begins and ends with square brackets.
     * @param text String to be parsed
     * @return A String formatted to fit a sign
     */
    public static String truncate(String text) {
        if (text.matches(patternBracketTooLong))
            return "[" + text.substring(1, 14) + "]";
        if (text.matches(patternNormalTooLong))
            return text.substring(0, 15);
        return text;
    }

    /**
     * Interacts with a (set) of doors, toggling their state if authorized.
     * @param player The player who clicked
     * @param block The block clicked
     * @param override Disregard signs and toggle regardless
     * @return Success or failure
     */
    public static boolean interactDoor(Player player, Block block, boolean override) {
        Block owner = Util.getOwnerSign(block);
        if (owner == null)
            return true;
        if (!override) {
            if (!Util.isAuthorized(player, block))
                if (player.hasPermission(Perm.admin_bypass)) {
                    sendMessage(player, String.format(Config.msg_admin_bypass, stripColor(((Sign) owner.getState()).getLine(1))), ChatColor.RED);
                } else
                    return false;
        }
        int delay = getDelayFromSign((Sign) owner.getState());
        boolean isNatural = isNaturalOpen(block);
        if (!isNatural && Config.doorSounds)
            block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
        Set<Block> doorBlocks = new HashSet<>();
        toggleDoor(block, isNatural, doorBlocks);
        if (delay > 0)
            doorSchedule.add(doorBlocks, delay, isNatural, block.getLocation());
        else if (Config.timerDoorsAlwaysOn && delay == -1)
            doorSchedule.add(doorBlocks, Config.timerDoorsAlwaysOnDelay, isNatural, block.getLocation());
        return true;
    }

    private static void toggleDoor(Block block, boolean naturalOpen, Set<Block> doorBlocks) {
        if (!doorBlocks.add(block))
            return;
        if (!naturalOpen)
            toggleSingleBlock(block);

        switch (block.getType()) {
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
                for (BlockFace bf : Util.allBlockFaces) {
                    Block adjacent = block.getRelative(bf);
                    if (adjacent.getType().equals(block.getType())) {
                        toggleDoor(adjacent, false, doorBlocks);
                    }
                }
                break;
            case TRAP_DOOR:
                for (BlockFace bf : Util.horizontalBlockFaces) {
                    Block adjacent = block.getRelative(bf);
                    if (adjacent.getType().equals(block.getType()))
                        toggleDoor(adjacent, false, doorBlocks);
                }
                break;
            case FENCE_GATE:
                for (BlockFace bf : Util.allBlockFaces) {
                    Block adjacent = block.getRelative(bf);
                    if (adjacent.getType().equals(block.getType())) {
                        toggleDoor(adjacent, false, doorBlocks);
                    }
                }
                break;
        }
    }

    private static void toggleSingleBlock(Block block) {
        block.setData((byte) (block.getData() ^ 0x4));
    }

    private static boolean isNaturalOpen(Block block) {
        switch (block.getType()) {
            case IRON_DOOR_BLOCK:
                return false;
            case WOODEN_DOOR:
            case TRAP_DOOR:
            case FENCE_GATE:
            default:
                return true;
        }
    }

    private static int getDelayFromSign(Sign sign) {
        for (int i = 2; i < 4; i++) {
            String text = Util.stripColor(sign.getLine(i));
            if (text.matches(timerPattern)) {
                String word = text.substring(1, text.length() - 3);
                if (word.equalsIgnoreCase(Config.signtext_timer) || word.equalsIgnoreCase(Config.signtext_timer_locale))
                    return Integer.valueOf(text.substring(text.length() - 2, text.length() - 1));
            }
        }
        return -1;
    }

    /**
     * Interacts with a container, opening if authorized
     * @param player The player who clicked
     * @param block The block clicked
     * @param override Disregard signs and toggle regardless
     * @return Success or failure
     */
    public static boolean interactContainer(Player player, Block block, boolean override) {
        if (override)
            return true;
        String owner = Util.getOwnerName(block);
        if (owner.isEmpty())
            return true;
        if (!Util.isAuthorized(player, block))
            if (player.hasPermission(Perm.admin_snoop)) {
                Util.sendBroadcast(Perm.admin_broadcast_snoop,
                        String.format(Config.msg_admin_snoop, player.getName(), owner),
                        ChatColor.RED);
                Deadbolt.logger.log(Level.INFO, String.format("Deadbolt: " + Config.msg_admin_snoop, player.getName(), owner));
            } else
                return false;
        return true;
    }

    /**
     * Interacts with a sign, selecting it for /deadbolt <line> <text> usage if authorized
     * @param player The player who clicked
     * @param block The block clicked
     * @return Success or failure
     */
    public static boolean interactSign(Player player, Block block) {
        String owner = Util.getOwnerName(block);
        if (owner.isEmpty())
            return true;
        if (!owner.equalsIgnoreCase(player.getName()))
            if (Config.adminSign && player.hasPermission(Perm.admin_signs))
                Util.sendMessage(player, String.format(Config.msg_admin_signs, owner), ChatColor.RED);
            else
                return false;
        Util.sendMessage(player, Config.cmd_sign_selected, ChatColor.GOLD);
        selectedSign.put(player, block);
        return true;
    }

    /**
     * Send <msg> to <player> in <color>
     * @param player Player to send <msg> to
     * @param msg The message to be sent
     * @param color Message coloring
     */
    public static void sendMessage(Player player, String msg, ChatColor color) {
        if (msg.equals(""))
            return;
        player.sendMessage(color + pluginTag + msg);
    }

    /**
     * Send <msg> to all players with <perm> in <color>
     * @param perm The permission to check
     * @param msg The message to be sent
     * @param color Message coloring
     */
    public static void sendBroadcast(String perm, String msg, ChatColor color) {
        if (!msg.isEmpty())
            for (Player player : Bukkit.getServer().getOnlinePlayers())
                if (player.hasPermission(perm))
                    player.sendMessage(color + pluginTag + msg);

    }
}
