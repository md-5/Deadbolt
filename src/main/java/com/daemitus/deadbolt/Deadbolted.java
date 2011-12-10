package com.daemitus.deadbolt;

import com.daemitus.deadbolt.tasks.ToggleDoorTask;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Furnace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.material.Door;
import org.bukkit.material.TrapDoor;

public class Deadbolted {

    public Set<Block> blocks = new HashSet<Block>();
    private Set<Block> traversed = new HashSet<Block>();
    private String owner = null;
    private Set<String> users = new HashSet<String>();
    private static Deadbolt plugin;

    public Deadbolted(final Deadbolt plugin) {
        Deadbolted.plugin = plugin;
    }

    private Deadbolted(Block block) {
        search(block);
    }

    private void search(Block block) {

        switch (block.getType()) {
            case AIR:
                break;
            case WALL_SIGN:
                BlockState state = block.getState();
                org.bukkit.block.Sign signState = (Sign) state;
                if (Config.isValidWallSign(signState))
                    search(Config.getSignAttached(signState));
                break;
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
                searchDoor(block, true, true);
                break;
            case FENCE_GATE:
                searchFenceGate(block, true, true);
                break;
            case TRAP_DOOR:
                searchTrapDoor(block, true, Config.vertical_trapdoors);
                break;
            case DISPENSER:
                searchDispenser(block, Config.group_dispensers, Config.group_dispensers);
                break;
            case FURNACE:
            case BURNING_FURNACE:
                searchFurnace(block, Config.group_furnaces, Config.group_furnaces);
                break;
            case CHEST:
                searchChest(block, true, true);
                break;
            default:
                for (BlockFace bf : Config.CARDINAL_FACES) {
                    Block adjacent = block.getRelative(bf);
                    if (adjacent.getState().getData() instanceof TrapDoor) {
                        Block hinge = adjacent.getRelative(((TrapDoor) adjacent.getState().getData()).getAttachedFace());
                        if (hinge.equals(block))
                            search(adjacent);
                    }
                }
                Block adjacentUp = block.getRelative(BlockFace.UP);
                if (adjacentUp.getState().getData() instanceof Door) {
                    search(adjacentUp);
                }
                Block adjacentDown = block.getRelative(BlockFace.DOWN);
                if (adjacentDown.getState().getData() instanceof Door) {
                    search(adjacentDown);
                }
        }
    }

    private void searchDoor(Block block, boolean horizontal, boolean vertical) {
        if (!add(block))
            return;
        for (BlockFace bf : Config.CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            if (horizontal && adjacent.getState().getData() instanceof Door) {
                searchDoor(adjacent, horizontal, vertical);
            } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
                parseSignAttached(adjacent, block);
            }
        }
        if (vertical) {
            Block adjacentUp = block.getRelative(BlockFace.UP);
            if (adjacentUp.getState().getData() instanceof Door) {
                searchDoor(adjacentUp, horizontal, vertical);
            } else {
                parseNearbySigns(adjacentUp);
            }
            //Get the base block, regardless of type
            Block adjacentDown = block.getRelative(BlockFace.DOWN);
            if (adjacentDown.getState().getData() instanceof Door) {
                searchDoor(adjacentDown, horizontal, vertical);
            } else {
                parseNearbySigns(adjacentDown);
                add(adjacentDown);
            }
        }
    }

    private void searchFenceGate(Block block, boolean horizontal, boolean vertical) {
        if (!add(block))
            return;
        for (BlockFace bf : Config.CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            if (horizontal && adjacent.getType().equals(Material.FENCE_GATE)) {
                searchFenceGate(adjacent, horizontal, vertical);
            } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
                parseSignAttached(adjacent, block);
            } else {
                parseNearbySigns(adjacent);
            }
        }
        if (vertical)
            for (BlockFace bf : Config.VERTICAL_FACES) {
                Block adjacent = block.getRelative(bf);
                if (adjacent.getType().equals(Material.FENCE_GATE)) {
                    searchFenceGate(adjacent, horizontal, vertical);
                }
            }
    }

    private void searchTrapDoor(Block block, boolean horizontal, boolean vertical) {
        if (!add(block))
            return;
        Block hinge = block.getRelative(((TrapDoor) block.getState().getData()).getAttachedFace());
        parseNearbySigns(hinge);
        add(hinge);
        for (BlockFace bf : Config.CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            if (horizontal && adjacent.getState().getData() instanceof TrapDoor) {
                searchTrapDoor(adjacent, horizontal, vertical);
            } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
                parseSignAttached(adjacent, block);
            }
        }
        if (vertical)
            for (BlockFace bf : Config.VERTICAL_FACES) {
                Block adjacent = block.getRelative(bf);
                if (adjacent.getState().getData() instanceof TrapDoor) {
                    searchTrapDoor(adjacent, horizontal, vertical);
                } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
                    BlockState state = adjacent.getState();
                    org.bukkit.material.Sign signData = (org.bukkit.material.Sign) state.getData();
                    Block attached = adjacent.getRelative(signData.getAttachedFace());
                    if (parseSign((Sign) state))
                        add(adjacent, attached);
                }
            }
    }

    private void searchDispenser(Block block, boolean horizontal, boolean vertical) {
        if (!add(block))
            return;
        for (BlockFace bf : Config.CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            if (horizontal && adjacent.getState() instanceof Dispenser) {
                searchDispenser(adjacent, horizontal, vertical);
            } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
                parseSignAttached(adjacent, block);
            }
        }
        if (vertical)
            for (BlockFace bf : Config.VERTICAL_FACES) {
                Block adjacent = block.getRelative(bf);
                if (adjacent.getState() instanceof Dispenser) {
                    searchDispenser(adjacent, horizontal, vertical);
                }
            }
    }

    private void searchFurnace(Block block, boolean horizontal, boolean vertical) {
        if (!add(block))
            return;
        for (BlockFace bf : Config.CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            if (horizontal && adjacent.getState() instanceof Furnace) {
                searchFurnace(adjacent, horizontal, vertical);
            } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
                parseSignAttached(adjacent, block);
            }
        }
        if (vertical)
            for (BlockFace bf : Config.VERTICAL_FACES) {
                Block adjacent = block.getRelative(bf);
                if (adjacent.getState() instanceof Furnace) {
                    searchFurnace(adjacent, horizontal, vertical);
                }
            }
    }

    private void searchChest(Block block, boolean horizontal, boolean vertical) {
        if (!add(block))
            return;
        for (BlockFace bf : Config.CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            if (horizontal && adjacent.getState() instanceof Chest) {
                searchChest(adjacent, horizontal, vertical);
            } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
                parseSignAttached(adjacent, block);
            }
        }
        if (vertical)
            for (BlockFace bf : Config.VERTICAL_FACES) {
                Block adjacent = block.getRelative(bf);
                if (adjacent.getState() instanceof Chest) {
                    searchChest(adjacent, horizontal, vertical);
                }
            }
    }

    private void parseNearbySigns(Block block) {
        for (BlockFace bf : Config.CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            if (adjacent.getState() instanceof Sign)
                parseSignAttached(adjacent, block);
        }
    }

    private void parseSignAttached(Block signBlock, Block attached) {
        if (signBlock.getRelative(Config.getFacingFromByte(signBlock.getData()).getOppositeFace()).equals(attached))
            if (parseSign((Sign) signBlock.getState())) {
                add(attached, signBlock);
            }
    }

    private boolean parseSign(Sign sign) {
        String ident = Config.getLine(sign, 0);
        if (Config.isPrivate(ident)) {
            String line1 = Config.getLine(sign, 1).toLowerCase();
            owner = line1.isEmpty() ? owner : line1;
            users.add(Config.getLine(sign, 2).toLowerCase());
            users.add(Config.getLine(sign, 3).toLowerCase());
            return true;
        } else if (Config.isMoreUsers(ident)) {
            users.add(Config.getLine(sign, 1).toLowerCase());
            users.add(Config.getLine(sign, 2).toLowerCase());
            users.add(Config.getLine(sign, 3).toLowerCase());
            return true;
        }
        return false;
    }

    public static Deadbolted get(Block block) {
        return new Deadbolted(block);
    }

    public boolean isProtected() {
        return owner != null && !owner.isEmpty();
    }

    public boolean isOwner(Player player) {
        return owner.equalsIgnoreCase(Config.truncateName(player.getName()));
    }

    public boolean isUser(Player player) {
        return isOwner(player)
                || users.contains(Config.truncateName(player.getName().toLowerCase()))
                || isEveryone();
    }

    public boolean isEveryone() {
        for (String line : users)
            if (Config.isEveryone(line))
                return true;
        return false;
    }

    public int getTimer() {
        for (String line : users) {
            int timer = Config.getTimer(line);
            if (timer != -1) {
                return timer;
            }
        }
        return -1;
    }

    private boolean add(Block... block) {
        boolean success = true;
        for (Block b : block)
            success &= blocks.add(b) && traversed.add(b);
        return success;
    }

    public String getOwner() {
        return owner;
    }

    public List<String> getUsers() {
        return new ArrayList<String>(users);
    }

    public void toggleDoors(Block block) {

        Set<Block> clickedDoor = new HashSet<Block>();
        if (isNaturalOpen(block)) {
            clickedDoor.add(block);
            if (isVerticallyJoined(block)) {
                Block b = block;
                while ((b = b.getRelative(BlockFace.UP)).getType().equals(block.getType()))
                    clickedDoor.add(b);
                b = block;
                while ((b = b.getRelative(BlockFace.DOWN)).getType().equals(block.getType()))
                    clickedDoor.add(b);
            }
        }

        List<Block> validToggles = new ArrayList<Block>(blocks);
        validToggles.removeAll(clickedDoor);
        for (Block b : validToggles)
            if (b.getType().equals(block.getType()))
                toggle(b);

        if (!isNaturalSound(block) && Config.silent_door_sounds)
            block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 10);



        if (Config.deny_timed_doors)
            return;
        int delay = getTimer();
        if (delay == -1)
            if (Config.forced_timed_doors)
                delay = Config.forced_timed_doors_delay;
            else
                return;
        validToggles.addAll(clickedDoor);
        boolean runonce = true;
        for (Block bl : validToggles) {
            if (ToggleDoorTask.timedBlocks.containsKey(bl)) {
                plugin.getServer().getScheduler().cancelTask(ToggleDoorTask.timedBlocks.get(bl));
            } else {
                ToggleDoorTask.timedBlocks.put(bl, plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ToggleDoorTask(bl)));
                if (runonce && Config.timed_door_sounds && isNaturalSound(block) ? true : Config.silent_door_sounds) {
                    block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 10);
                }
            }
        }
    }

    private boolean isNaturalOpen(Block block) {
        switch (block.getType()) {
            case WOODEN_DOOR:
            case TRAP_DOOR:
                return true;
            default:
                return false;
        }
    }

    private boolean isVerticallyJoined(Block block) {
        switch (block.getType()) {
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
                return true;
            case TRAP_DOOR:
                return Config.vertical_trapdoors;
            default:
                return false;

        }
    }

    private boolean isNaturalSound(Block block) {
        switch (block.getType()) {
            case IRON_DOOR_BLOCK:
                return false;
            default:
                return true;
        }
    }

    private void toggle(Block block) {
        block.setData((byte) (block.getData() ^ 0x4));
    }
}
