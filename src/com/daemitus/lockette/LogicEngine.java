package com.daemitus.lockette;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

public class LogicEngine {

    private final Lockette plugin;
    private final DoorSchedule doorSchedule;
    public final Set<BlockFace> horizontalBlockFaces = EnumSet.of(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST);
    public final Set<BlockFace> verticalBlockFaces = EnumSet.of(BlockFace.UP, BlockFace.DOWN);
    private HashMap<Player, Block> selectedSign = new HashMap<Player, Block>();

    public LogicEngine(final Lockette plugin) {
        this.plugin = plugin;
        doorSchedule = new DoorSchedule(plugin);
        doorSchedule.start();
    }

    public boolean isProtected(Block block) {
        return getOwnerSign(block) != null;
    }

    public boolean isOwner(String name, Block block) {
        return truncate(name).equalsIgnoreCase(getOwnerName(block));
    }

    public boolean isAuthorized(String name, Block block) {
        List<String> names = getAllNames(block);
        return !names.isEmpty() && (names.contains(truncate(name).toLowerCase())
                                    || names.contains(plugin.cm.ingame_sign_everyone)
                                    || names.contains(plugin.cm.ingame_sign_everyone_locale));
    }

    public String getOwnerName(Block block) {
        Block owner = getOwnerSign(block);
        if (owner == null)
            return "";
        Sign sign = (Sign) owner.getState();
        String text = stripColor(sign.getLine(1));
        return text;
    }

    public Block getOwnerSign(Block block) {
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
            if (text.equalsIgnoreCase(plugin.cm.ingame_sign_primary) || text.equalsIgnoreCase(plugin.cm.ingame_sign_primary_locale))
                return block;
            if (text.equalsIgnoreCase(plugin.cm.ingame_sign_moreusers) || text.equalsIgnoreCase(plugin.cm.ingame_sign_moreusers_locale)) {
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

    private Block getOwnerSign(Block block, boolean iterateHorizontal, boolean iterateVertical, boolean trapDoor) {
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
            if (!text.equalsIgnoreCase(plugin.cm.ingame_sign_primary) && !text.equalsIgnoreCase(plugin.cm.ingame_sign_primary_locale))
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

    public List<String> getAllNames(Block block) {
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

    private Set<Block> getAllSigns(Block block) {
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
            if (text.equalsIgnoreCase(plugin.cm.ingame_sign_primary) || text.equalsIgnoreCase(plugin.cm.ingame_sign_primary_locale))
                return getAllSigns(getBlockSignAttachedTo(block));
            if (text.equalsIgnoreCase(plugin.cm.ingame_sign_moreusers) || text.equalsIgnoreCase(plugin.cm.ingame_sign_moreusers_locale))
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

    private Set<Block> getAllSigns(Block block, boolean iterateHorizontal, boolean iterateVertical, boolean trapDoor) {
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
            if (text.equalsIgnoreCase(plugin.cm.ingame_sign_primary) || text.equalsIgnoreCase(plugin.cm.ingame_sign_primary_locale))
                signSet.add(adjacent);
            if (text.equalsIgnoreCase(plugin.cm.ingame_sign_moreusers) || text.equalsIgnoreCase(plugin.cm.ingame_sign_moreusers_locale))
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

    public Block getBlockSignAttachedTo(Block block) {
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

    public String stripColor(String line) {
        return line.replaceAll(plugin.cm.patternStripColor, "");
    }

    public String truncate(String text) {
        if (text.matches(plugin.cm.patternBracketTooLong))
            return "[" + text.substring(1, 14) + "]";
        if (text.matches(plugin.cm.patternNormalTooLong))
            return text.substring(0, 15);
        return text;
    }

    public void sendInfoMessage(Player player, String msg) {
        if (!msg.equals(""))
            player.sendMessage(plugin.cm.color_info + plugin.cm.pluginTag + ": " + msg);
    }

    public void sendErrorMessage(Player player, String msg) {
        if (!msg.equals(""))
            player.sendMessage(plugin.cm.color_error + plugin.cm.pluginTag + ": " + msg);
    }

    public boolean interactDoor(Player player, Block block) {
        Block owner = getOwnerSign(block);
        if (owner == null)
            return true;
        if (!isAuthorized(truncate(player.getName()), block))
            return false;
        Block ownerAttached = getBlockSignAttachedTo(owner);
        int delay = getDelayFromSign((Sign) owner.getState());
        Set<Block> doorBlocks = new HashSet<Block>();
        doorBlocks = toggleDoor(block, ownerAttached, isNaturalOpen(block));
        if (delay > 0) {
            doorSchedule.add(doorBlocks, delay);
        }
        return true;
    }

    private Set<Block> toggleDoor(Block block, Block keyBlock, boolean naturalOpen) {
        Set<Block> set = new HashSet<Block>();
        set.add(block);
        if (!naturalOpen)
            toggleSingleBlock(block);

        for (BlockFace bf : verticalBlockFaces) {
            Block verticalBlock = block.getRelative(bf);
            if (verticalBlock.getType().equals(block.getType())) {
                set.add(verticalBlock);
                if (!naturalOpen)
                    toggleSingleBlock(verticalBlock);
            }
        }

        if (keyBlock != null) {
            for (BlockFace bf : horizontalBlockFaces) {
                Block adjacent = block.getRelative(bf);
                if (adjacent.getType().equals(block.getType())
                    && ((adjacent.getX() == keyBlock.getX() && adjacent.getZ() == keyBlock.getZ())
                        || (block.getX() == keyBlock.getX() && block.getZ() == keyBlock.getZ())))
                    set.addAll(toggleDoor(adjacent, null, false));
            }
        }
        return set;
    }

    private Block toggleSingleBlock(Block block) {
        block.setData((byte) (block.getData() ^ 0x4));
        return block;
    }

    private boolean isNaturalOpen(Block block) {
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

    private int getDelayFromSign(Sign sign) {
        for (int i = 2; i < 4; i++) {
            String text = sign.getLine(i);
            if (!text.matches(plugin.cm.patternTimer))
                continue;

            String word = text.substring(1, text.length() - 3);
            if (!word.equalsIgnoreCase(plugin.cm.ingame_sign_timer) && !word.equalsIgnoreCase(plugin.cm.ingame_sign_timer_locale))
                continue;

            return Integer.valueOf(text.substring(text.length() - 2, text.length() - 1));
        }
        return 0;
    }

    public void shutdown() {
        selectedSign.clear();
        doorSchedule.stop();
    }

    public boolean interactContainer(Player player, Block block) {
        Block owner = getOwnerSign(block);
        if (owner == null)
            return true;
        if (!isAuthorized(truncate(player.getName()), block))
            return false;
        return true;
    }

    public boolean interactSign(Player player, Block block) {
        String owner = getOwnerName(block);
        if (owner.equals("")) {
            return true;
        }
        if (!owner.equalsIgnoreCase(stripColor(player.getName()))) {
            return false;
        }
        selectedSign.put(player, block);
        sendInfoMessage(player, plugin.cm.msg_user_sign_selected_locale);
        return true;
    }

    public Block getSelectedSign(Player player) {
        return selectedSign.get(player);
    }

    public void clearSelectedSign(Player player) {
        selectedSign.remove(player);
    }
}
