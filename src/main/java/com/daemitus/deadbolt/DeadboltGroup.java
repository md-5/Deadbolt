package com.daemitus.deadbolt;

import com.daemitus.deadbolt.bridge.Bridge;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.material.TrapDoor;

public class DeadboltGroup {

    private static final Set<Block> toggledBlocks = new HashSet<Block>();
    private String owner = null;
    private List<String> authorized = new ArrayList<String>();
    private Set<Block> related = new HashSet<Block>();
    private Set<Block> traversed = new HashSet<Block>();
    public int timer = Conf.defaultTimer;

    public DeadboltGroup() {
    }

    public boolean add(Block block) {
        boolean r1 = traversed.add(block);
        boolean r2 = related.add(block);
        return r1 && r2;
    }

    public static DeadboltGroup getRelated(Block block) {
        return getRelated(block, new DeadboltGroup());
    }

    public static DeadboltGroup getRelated(Block block, DeadboltGroup dbg) {
        Block adjacent, attached;
        switch (block.getType()) {
            case AIR:
                break;
            case WALL_SIGN:
                if (isValidWallSign((Sign) block.getState())) {
                    attached = getBlockSignAttachedTo(block);
                    getRelated(attached, dbg);
                }
                break;
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
            case FENCE_GATE:
                getRelated(block, true, true, dbg);
                break;
            case TRAP_DOOR:
                getRelated(block, true, Conf.verticalTrapdoors, dbg);
                break;
            case DISPENSER:
            case FURNACE:
            case BURNING_FURNACE:
                getRelated(block, Conf.groupContainers, Conf.groupContainers, dbg);
                break;
            case CHEST:
                getRelated(block, true, true, dbg);
                break;
            default:
                for (BlockFace bf : Conf.VERTICAL_FACES) {
                    adjacent = block.getRelative(bf);
                    switch (adjacent.getType()) {
                        case WOODEN_DOOR:
                        case IRON_DOOR_BLOCK:
                            getRelated(adjacent, dbg);
                    }
                }

                boolean isProtected = false;
                for (BlockFace bf : Conf.CARDINAL_FACES) {
                    adjacent = block.getRelative(bf);
                    switch (adjacent.getType()) {
                        case TRAP_DOOR:
                            TrapDoor trap = (TrapDoor) adjacent.getState().getData();
                            Block hinge = adjacent.getRelative(trap.getAttachedFace());
                            if (hinge.equals(block))
                                getRelated(adjacent, dbg);
                            break;
                        case WALL_SIGN:
                            if (isValidWallSign((Sign) adjacent.getState())) {
                                isProtected = true;
                                if (adjacent.getRelative(BlockFace.UP).getType().equals(Material.TRAP_DOOR))
                                    getRelated(adjacent.getRelative(BlockFace.UP), dbg);
                                if (adjacent.getRelative(BlockFace.DOWN).getType().equals(Material.TRAP_DOOR))
                                    getRelated(adjacent.getRelative(BlockFace.DOWN), dbg);
                            }
                    }
                }
                if (isProtected) {
                    for (BlockFace bf : Conf.CARDINAL_FACES) {
                        adjacent = block.getRelative(bf);
                        switch (adjacent.getType()) {
                            case FENCE_GATE:
                                getRelated(adjacent, dbg);
                        }
                    }
                }
        }
        return dbg;
    }

    public static void getRelated(Block block, boolean iterateHorizontal, boolean iterateVertical, DeadboltGroup dbg) {
        if (!dbg.add(block))
            return;
        switch (block.getType()) {
            case TRAP_DOOR:
                TrapDoor trap = (TrapDoor) block.getState().getData();
                Block hinge = block.getRelative(trap.getAttachedFace());
                dbg.add(hinge);
                parseAdjacent(hinge, dbg);
                parseSignBlock(block.getRelative(BlockFace.UP), dbg);
                parseSignBlock(block.getRelative(BlockFace.DOWN), dbg);
                break;
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
                Block baseBlock = block;
                while (baseBlock.getType().equals(block.getType()))
                    baseBlock = baseBlock.getRelative(BlockFace.DOWN);
                dbg.add(baseBlock);
                parseAdjacent(baseBlock, dbg);
                break;
        }

        for (BlockFace bf : Conf.CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            switch (adjacent.getType()) {
                case WALL_SIGN:
                    parseSignAttached(adjacent, block, dbg);
                    break;
                case WOODEN_DOOR:
                case IRON_DOOR_BLOCK:
                case FENCE_GATE:
                case TRAP_DOOR:
                case DISPENSER:
                case FURNACE:
                case BURNING_FURNACE:
                case CHEST:
                    if (iterateHorizontal && adjacent.getType().equals(block.getType()))
                        getRelated(adjacent, iterateHorizontal, iterateVertical, dbg);
                    break;
                default:
                    switch (block.getType()) {
                        case WOODEN_DOOR:
                        case IRON_DOOR_BLOCK:
                        case FENCE_GATE:
                            parseAdjacent(adjacent, dbg);
                    }

            }
        }

        if (iterateVertical) {
            for (BlockFace bf : Conf.VERTICAL_FACES) {
                Block adjacent = block.getRelative(bf);
                switch (adjacent.getType()) {
                    case WALL_SIGN:
                    case WOODEN_DOOR:
                    case IRON_DOOR_BLOCK:
                    case FENCE_GATE:
                    case TRAP_DOOR:
                    case DISPENSER:
                    case FURNACE:
                    case BURNING_FURNACE:
                    case CHEST:
                        if (adjacent.getType().equals(block.getType()))
                            getRelated(adjacent, iterateHorizontal, iterateVertical, dbg);
                        break;
                    default:
                        switch (block.getType()) {
                            case WOODEN_DOOR:
                            case IRON_DOOR_BLOCK:
                            case FENCE_GATE:
                                parseAdjacent(adjacent, dbg);
                        }
                }
            }
        }
    }

    public static boolean parseAdjacent(Block attached, DeadboltGroup dbg) {
        boolean added = false;
        for (BlockFace bf : Conf.CARDINAL_FACES) {
            Block signBlock = attached.getRelative(bf);
            if (!signBlock.getType().equals(Material.WALL_SIGN)
                    || (bf.equals(BlockFace.EAST) && signBlock.getData() != 2)
                    || (bf.equals(BlockFace.WEST) && signBlock.getData() != 3)
                    || (bf.equals(BlockFace.NORTH) && signBlock.getData() != 4)
                    || (bf.equals(BlockFace.SOUTH) && signBlock.getData() != 5))
                continue;
            Sign sign = (Sign) signBlock.getState();
            added = added || parseSign(sign, dbg);
        }
        if (added)
            dbg.add(attached);
        return added;
    }

    public static boolean parseSignAttached(Block signBlock, Block attached, DeadboltGroup dbg) {
        if (signBlock.getType().equals(Material.WALL_SIGN)
                && ((signBlock.getData() == 2 && signBlock.getRelative(BlockFace.WEST).equals(attached))
                || (signBlock.getData() == 3 && signBlock.getRelative(BlockFace.EAST).equals(attached))
                || (signBlock.getData() == 4 && signBlock.getRelative(BlockFace.SOUTH).equals(attached))
                || (signBlock.getData() == 5 && signBlock.getRelative(BlockFace.NORTH).equals(attached)))) {
            Sign sign = (Sign) signBlock.getState();
            if (parseSign(sign, dbg)) {
                dbg.add(attached);
                return true;
            }
        }
        return false;
    }

    public static boolean parseSignBlock(Block signBlock, DeadboltGroup dbg) {
        if (signBlock.getType().equals(Material.WALL_SIGN)) {
            Sign sign = (Sign) signBlock.getState();
            return parseSign(sign, dbg);
        }
        return false;
    }

    public static boolean parseSign(Sign sign, DeadboltGroup dbg) {
        String line = Conf.getLine(sign, 0);
        if (Conf.isPrivate(line)) {
            dbg.owner = Conf.getLine(sign, 1);
            for (int i = 2; i < 4; i++) {
                line = Conf.getLine(sign, i);
                if (Conf.isTimer(line))
                    dbg.timer = Integer.valueOf(line.substring(line.length() - 2, line.length() - 1));
                else
                    dbg.authorized.add(line);
            }
            dbg.add(sign.getBlock());
            return true;
        } else if (Conf.isMoreUsers(line)) {
            for (int i = 1; i < 4; i++) {
                dbg.authorized.add(line);
            }
            dbg.add(sign.getBlock());
            return true;
        }
        return false;
    }

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

    public static boolean isValidWallSign(Sign sign) {
        return Conf.isPrivate(sign.getLine(0)) || Conf.isMoreUsers(sign.getLine(0));
    }

    public static boolean isValidWallSign(Block signBlock) {
        if (signBlock.getType().equals(Material.WALL_SIGN)) {
            Sign sign = (Sign) signBlock.getState();
            return Conf.isPrivate(sign.getLine(0)) || Conf.isMoreUsers(sign.getLine(0));
        }
        return false;
    }

    public boolean contains(Block block) {
        return related.contains(block);
    }

    public String getOwner() {
        return owner;
    }

    public boolean isOwner(Player player) {
        if (owner == null)
            return false;
        if (owner.equalsIgnoreCase(Conf.truncate(player.getName(), 13)))
            return true;
        return false;
    }

    public boolean isOwnerOrNull(Player player) {
        return owner == null || isOwner(player);
    }

    public Set<Block> getSubset(Material targetType) {
        Set<Block> set = new HashSet<Block>();
        for (Block block : related)
            if (block.getType().equals(targetType))
                set.add(block);
        return set;
    }

    public boolean isAuthorized(Player player) {
        if (player == null) {//null? check for everyone
            for (String text : authorized)
                if (Conf.isEveryone(text))
                    return true;
        } else if (isOwner(player) || authorized.contains(Conf.truncate(player.getName(), 13)) || Bridge.isAuthorized(player, authorized))
            return true;

        return false;
    }

    public void toggleBlocks(final Deadbolt plugin, Material type) {
        Set<Block> doorBlocks = getSubset(type);
        boolean playOnce = Conf.silentDoorSounds && !hasNaturalSound(type);
        for (Block block : doorBlocks) {
            if (block.getType().equals(type)) {
                block.setData((byte) (block.getData() ^ 0x4));
                if (playOnce) {
                    block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 10);
                    playOnce = false;
                }
            }
        }

        if (timer > 0)
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new DoorTask(type, doorBlocks), timer * 20);

    }

    private boolean hasNaturalSound(Material type) {
        switch (type) {
            case IRON_DOOR_BLOCK:
                return false;
            case WOODEN_DOOR:
            case TRAP_DOOR:
            case FENCE_GATE:
            default:
                return true;
        }
    }

    public List<String> getAuthorized() {
        return authorized;
    }

    private class DoorTask implements Runnable {

        private final Material type;
        private final Set<Block> doorBlocks;

        public DoorTask(Material type, Set<Block> doorBlocks) {
            this.type = type;
            this.doorBlocks = doorBlocks;
            for (Block block : doorBlocks) {
                if (!toggledBlocks.add(block))
                    toggledBlocks.remove(block);
            }
        }

        @Override
        public void run() {
            boolean playOnce = Conf.timedDoorSounds && (hasNaturalSound(type) || Conf.silentDoorSounds);
            for (Block block : doorBlocks) {
                if (toggledBlocks.remove(block) && block.getType().equals(type)) {
                    block.setData((byte) (block.getData() ^ 0x4));
                    if (playOnce) {
                        block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 10);
                        playOnce = false;
                    }
                }
            }
        }
    }
}
