package com.daemitus.lockette;

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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.ContainerBlock;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;
import org.bukkit.material.TrapDoor;

public class Util {

    public static final Set<BlockFace> horizontalBlockFaces = EnumSet.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);
    public static final Set<BlockFace> verticalBlockFaces = EnumSet.of(BlockFace.UP, BlockFace.DOWN);
    public static final Map<Player, Block> selectedSign = new HashMap<Player, Block>();
    private static final String pluginTag = "Lockette: ";
    private static final String patternStripColor = "(?i)§[0-9a-zA-Z]";
    private static final String patternNormalTooLong = ".{16,}";
    private static final String patternBracketTooLong = "\\[.{14,}\\]";
    private static final String timerPattern = "\\[.{1,11}:[123456789]\\]";
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
    public static boolean isAuthorized(String name, Block block) {
        List<String> names = getAllNames(block);
        if (names.isEmpty())
            return true;
        else
            return names.contains(truncate(name).toLowerCase())
                   || names.contains(Config.signtext_everyone)
                   || names.contains(Config.signtext_everyone_locale);
    }

    /**
     * Retrieves owner of <block>
     * @param block Block to be checked
     * @return The text on the line below [Private] on the sign associated with <block>. "" if unprotected
     */
    public static String getOwnerName(Block block) {
        Block owner = getOwnerSign(block);
        if (owner == null)
            return "";
        Sign sign = (Sign) owner.getState();
        String text = stripColor(sign.getLine(1));
        return text;
    }

    /**
     * Retrieves the sign block associated with <block>
     * @param block Block to be checked
     * @return The sign block associated with <block>. Null if unprotected
     */
    public static Block getOwnerSign(Block block) {
        Material type = block.getType();
        BlockState state = block.getState();
        MaterialData data = state.getData();
        if (type.equals(Material.CHEST))
            return getOwnerSign(block, true, false, false);
        else if (state instanceof ContainerBlock)
            return getOwnerSign(block, false, false, false);
        else if (data instanceof Door)
            return getOwnerSign(block, true, true, false);
        else if (data instanceof TrapDoor)
            return getOwnerSign(block, false, false, true);
        else if (type.equals(Material.WALL_SIGN)) {
            Sign sign = (Sign) block.getState();
            String text = stripColor(sign.getLine(0));
            if (text.equalsIgnoreCase(Config.signtext_private) || text.equalsIgnoreCase(Config.signtext_private_locale))
                return block;
            if (text.equalsIgnoreCase(Config.signtext_moreusers) || text.equalsIgnoreCase(Config.signtext_moreusers_locale)) {
                Block attached = getBlockSignAttachedTo(block);
                return getOwnerSign(attached);
            }
            return null;
        } else {
            Block check = getOwnerSign(block, false, false, false);
            if (check == null) {
                Block upBlock = block.getRelative(BlockFace.UP);
                if (upBlock.getState().getData() instanceof Door)
                    return getOwnerSign(upBlock);
                Block downBlock = block.getRelative(BlockFace.DOWN);
                if (downBlock.getState().getData() instanceof Door)
                    return getOwnerSign(downBlock);
            }
            return check;
        }
    }

    private static Block getOwnerSign(Block block, boolean iterateHorizontal, boolean iterateVertical, boolean trapDoor) {
        if (trapDoor && block.getState().getData() instanceof TrapDoor) {
            TrapDoor door = (TrapDoor) block.getState().getData();
            block = block.getRelative(door.getAttachedFace());
        }

        for (BlockFace bf : horizontalBlockFaces) {
            Block adjacent = block.getRelative(bf);

            if (iterateHorizontal && adjacent.getType().equals(block.getType())) {
                adjacent = getOwnerSign(adjacent, false, iterateVertical, false);
                if (adjacent != null)
                    return adjacent;
                else
                    continue;
            }

            if (!adjacent.getType().equals(Material.WALL_SIGN))
                continue;
            if ((bf.equals(BlockFace.NORTH) && adjacent.getData() != 4)
                || (bf.equals(BlockFace.SOUTH) && adjacent.getData() != 5)
                || (bf.equals(BlockFace.EAST) && adjacent.getData() != 2)
                || (bf.equals(BlockFace.WEST) && adjacent.getData() != 3))
                continue;
            Sign sign = (Sign) adjacent.getState();
            String text = stripColor(sign.getLine(0));
            if (!text.equalsIgnoreCase(Config.signtext_private) && !text.equalsIgnoreCase(Config.signtext_private_locale))
                continue;
            return adjacent;
        }

        if (iterateVertical) {
            Set<Block> set = new HashSet<Block>();
            for (BlockFace bf : verticalBlockFaces) {
                Block iterating = block;
                while (iterating.getType().equals(block.getType())) {
                    iterating = iterating.getRelative(bf);
                    set.add(iterating);
                }
            }
            for (Block setBlock : set) {
                Block checkBlock = getOwnerSign(setBlock, false, false, false);
                if (checkBlock != null)
                    return checkBlock;
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
        List<String> names = new ArrayList<String>();
        for (Block signBlock : signs) {
            Sign sign = (Sign) signBlock.getState();
            for (int i = 1; i < 4; i++) {
                String line = sign.getLine(i);
                if (line.length() > 0)
                    names.add(stripColor(line).toLowerCase());
            }
        }
        return names;
    }

    private static Set<Block> getAllSigns(Block block) {
        Material type = block.getType();
        BlockState state = block.getState();
        MaterialData data = state.getData();
        if (type.equals(Material.CHEST))
            return getAllSigns(block, true, false, false);
        else if (state instanceof ContainerBlock)
            return getAllSigns(block, false, false, false);
        else if (data instanceof Door)
            return getAllSigns(block, true, true, false);
        else if (data instanceof TrapDoor)
            return getAllSigns(block, false, false, true);
        else if (type.equals(Material.WALL_SIGN)) {
            Sign sign = (Sign) block.getState();
            String text = stripColor(sign.getLine(0));
            if (text.equalsIgnoreCase(Config.signtext_private) || text.equalsIgnoreCase(Config.signtext_private_locale))
                return getAllSigns(getBlockSignAttachedTo(block));
            if (text.equalsIgnoreCase(Config.signtext_moreusers) || text.equalsIgnoreCase(Config.signtext_moreusers_locale))
                return getAllSigns(getBlockSignAttachedTo(block));
        } else {
            Block upBlock = block.getRelative(BlockFace.UP);
            if (upBlock.getState().getData() instanceof Door)
                return getAllSigns(upBlock);
            Block downBlock = block.getRelative(BlockFace.DOWN);
            if (downBlock.getState().getData() instanceof Door)
                return getAllSigns(downBlock);
        }
        return new HashSet<Block>();
    }

    private static Set<Block> getAllSigns(Block block, boolean iterateHorizontal, boolean iterateVertical, boolean trapDoor) {
        Set<Block> signSet = new HashSet<Block>();

        if (trapDoor && block.getState().getData() instanceof TrapDoor) {
            TrapDoor door = (TrapDoor) block.getState().getData();
            block = block.getRelative(door.getAttachedFace());
        }

        for (BlockFace bf : horizontalBlockFaces) {
            Block adjacent = block.getRelative(bf);

            if (iterateHorizontal && adjacent.getType().equals(block.getType()))
                signSet.addAll(getAllSigns(adjacent, false, iterateVertical, false));

            if (!adjacent.getType().equals(Material.WALL_SIGN))
                continue;
            if ((bf.equals(BlockFace.NORTH) && adjacent.getData() != 4)
                || (bf.equals(BlockFace.SOUTH) && adjacent.getData() != 5)
                || (bf.equals(BlockFace.EAST) && adjacent.getData() != 2)
                || (bf.equals(BlockFace.WEST) && adjacent.getData() != 3))
                continue;
            Sign sign = (Sign) adjacent.getState();
            String text = stripColor(sign.getLine(0));
            if (text.equalsIgnoreCase(Config.signtext_private) || text.equalsIgnoreCase(Config.signtext_private_locale))
                signSet.add(adjacent);
            if (text.equalsIgnoreCase(Config.signtext_moreusers) || text.equalsIgnoreCase(Config.signtext_moreusers_locale))
                signSet.add(adjacent);
        }

        if (iterateVertical) {
            Set<Block> set = new HashSet<Block>();
            for (BlockFace bf : verticalBlockFaces) {
                Block iterating = block;
                while (iterating.getType().equals(block.getType())) {
                    iterating = iterating.getRelative(bf);
                    set.add(iterating);
                }
            }
            for (Block setBlock : set)
                signSet.addAll(getAllSigns(setBlock, false, false, false));
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
            if (!Util.isAuthorized(player.getName(), block))
                if (player.hasPermission(Perm.admin_bypass)) {
                    sendMessage(player, String.format(Config.msg_admin_bypass, ((Sign) owner.getState()).getLine(1)), ChatColor.RED);
                } else
                    return false;
        }
        Block ownerAttached = Util.getBlockSignAttachedTo(owner);
        int delay = getDelayFromSign((Sign) owner.getState());
        Set<Block> doorBlocks = new HashSet<Block>();
        doorBlocks = toggleDoor(block, ownerAttached, isNaturalOpen(block));
        if (Config.timerDoorsAlwaysOn)
            doorSchedule.add(doorBlocks, delay == 0 ? Config.timerDoorsAlwaysOnDelay : delay);
        else if (delay > 0) {
            doorSchedule.add(doorBlocks, delay);
        }
        return true;
    }

    private static Set<Block> toggleDoor(Block block, Block keyBlock, boolean naturalOpen) {
        Set<Block> set = new HashSet<Block>();
        set.add(block);
        if (!naturalOpen)
            toggleSingleBlock(block);

        for (BlockFace bf : Util.verticalBlockFaces) {
            Block verticalBlock = block.getRelative(bf);
            if (verticalBlock.getType().equals(block.getType())) {
                set.add(verticalBlock);
                if (!naturalOpen)
                    toggleSingleBlock(verticalBlock);
            }
        }

        if (keyBlock != null) {
            for (BlockFace bf : Util.horizontalBlockFaces) {
                Block adjacent = block.getRelative(bf);
                if (adjacent.getType().equals(block.getType())
                    && ((adjacent.getX() == keyBlock.getX() && adjacent.getZ() == keyBlock.getZ())
                        || (block.getX() == keyBlock.getX() && block.getZ() == keyBlock.getZ())))
                    set.addAll(toggleDoor(adjacent, null, false));
            }
        }
        return set;
    }

    private static Block toggleSingleBlock(Block block) {
        block.setData((byte) (block.getData() ^ 0x4));
        return block;
    }

    private static boolean isNaturalOpen(Block block) {
        switch (block.getType()) {
            case WOODEN_DOOR:
                return true;
            case TRAP_DOOR:
                return true;
            case IRON_DOOR_BLOCK:
                return false;
            default:
                return true;
        }
    }

    private static int getDelayFromSign(Sign sign) {
        for (int i = 2; i < 4; i++) {
            String text = Util.stripColor(sign.getLine(i));
            if (!text.matches(timerPattern))
                continue;

            String word = text.substring(1, text.length() - 3);
            if (!word.equalsIgnoreCase(Config.signtext_timer) && !word.equalsIgnoreCase(Config.signtext_timer_locale))
                continue;

            return Integer.valueOf(text.substring(text.length() - 2, text.length() - 1));
        }
        return 0;
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
        if (owner.equals(""))
            return true;
        if (!Util.isAuthorized(player.getName(), block))
            if (player.hasPermission(Perm.admin_snoop)) {
                Util.sendBroadcast(Perm.admin_broadcast_snoop,
                                   String.format(Config.msg_admin_snoop, player.getName(), owner),
                                   ChatColor.RED);
                Lockette.logger.log(Level.INFO, String.format("Lockette - " + Config.msg_admin_snoop, player.getName(), owner));
            } else
                return false;
        return true;
    }

    /**
     * Interacts with a sign, selecting it for /lockette <line> <text> usage if authorized
     * @param player The player who clicked
     * @param block The block clicked
     * @return Success or failure
     */
    public static boolean interactSign(Player player, Block block) {
        String owner = Util.getOwnerName(block);
        if (owner.equals(""))
            return false;
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
        if (msg.equals(""))
            return;
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.hasPermission(perm))
                player.sendMessage(color + pluginTag + msg);
        }
    }
}
