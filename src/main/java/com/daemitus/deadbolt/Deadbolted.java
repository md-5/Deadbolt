package com.daemitus.deadbolt;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.material.Attachable;
import org.bukkit.material.TrapDoor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Deadbolted {

    private Set<Block> blocks = new HashSet<Block>();
    private Set<Block> traversed = new HashSet<Block>();
    private String owner = null;
    private Set<String> users = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    public static DeadboltPlugin plugin;

    public Deadbolted(Block block) {
        search(block);
    }

    private void search(Block block) {

        switch (block.getType()) {
            case AIR:
                break;
            case WALL_SIGN:
                BlockState state = block.getState();
                org.bukkit.block.Sign signState = (Sign) state;
                if (Deadbolt.getLanguage().isValidWallSign(signState)) {
                    search(Util.getSignAttached(signState));
                }
                break;
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case JUNGLE_DOOR:
            case ACACIA_DOOR:
            case DARK_OAK_DOOR:
                searchDoor(block, true, true);
                break;
            case FENCE_GATE:
            case BIRCH_FENCE_GATE:
            case ACACIA_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case SPRUCE_FENCE_GATE:
                searchFenceGate(block, true, true);
                break;
            case TRAP_DOOR:
            case IRON_TRAPDOOR:
                searchTrapDoor(block, true, Deadbolt.getConfig().vertical_trapdoors);
                break;
            case DISPENSER:
                searchSimpleBlock(block, Deadbolt.getConfig().group_dispensers, Deadbolt.getConfig().group_dispensers);
                break;
            case BREWING_STAND:
                searchSimpleBlock(block, Deadbolt.getConfig().group_brewing_stands, Deadbolt.getConfig().group_brewing_stands);
                break;
            case ENCHANTMENT_TABLE:
                searchSimpleBlock(block, Deadbolt.getConfig().group_enchantment_tables, Deadbolt.getConfig().group_enchantment_tables);
                break;
            case CAULDRON:
                searchSimpleBlock(block, Deadbolt.getConfig().group_cauldrons, Deadbolt.getConfig().group_cauldrons);
                break;
            case FURNACE:
            case BURNING_FURNACE:
                searchFurnace(block, Deadbolt.getConfig().group_furnaces, Deadbolt.getConfig().group_furnaces);
                break;
            case TRAPPED_CHEST:
            case CHEST:
                searchChest(block, true, false);
                break;
            default:
                for (BlockFace bf : Deadbolt.getConfig().CARDINAL_FACES) {
                    Block adjacent = block.getRelative(bf);
                    if (adjacent.getState().getData() instanceof TrapDoor) {
                        Block hinge = adjacent.getRelative(((TrapDoor) adjacent.getState().getData()).getAttachedFace());
                        if (hinge.equals(block)) {
                            search(adjacent);
                        }
                    }
                }
                Block adjacentUp = block.getRelative(BlockFace.UP);
                switch (adjacentUp.getType()) {
                    // adjacentUp.getState().getData() instanceof Door no longer works for new doors
                    case WOODEN_DOOR:
                    case IRON_DOOR_BLOCK:
                    case SPRUCE_DOOR:
                    case BIRCH_DOOR:
                    case JUNGLE_DOOR:
                    case ACACIA_DOOR:
                    case DARK_OAK_DOOR:
                        search(adjacentUp);
                        break;
                }
                Block adjacentDown = block.getRelative(BlockFace.DOWN);
                switch (adjacentDown.getType()) {
                    // adjacentUp.getState().getData() instanceof Door no longer works for new doors
                    case WOODEN_DOOR:
                    case IRON_DOOR_BLOCK:
                    case SPRUCE_DOOR:
                    case BIRCH_DOOR:
                    case JUNGLE_DOOR:
                    case ACACIA_DOOR:
                    case DARK_OAK_DOOR:
                        search(adjacentDown);
                        break;
                }
        }
    }

    private void searchDoor(Block block, boolean horizontal, boolean vertical) {
        if (!add(block)) {
            return;
        }
        for (BlockFace bf : Deadbolt.getConfig().CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            if (horizontal && adjacent.getType().equals(block.getType())) {
                searchDoor(adjacent, horizontal, vertical);
            } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
                parseSignAttached(adjacent, block);
            }
        }
        if (vertical) {
            Block adjacentUp = block.getRelative(BlockFace.UP);
            if (adjacentUp.getType().equals(block.getType())) {
                searchDoor(adjacentUp, horizontal, vertical);
            } else {
                parseNearbySigns(adjacentUp);
            }
            //Get the base block, regardless of type
            Block adjacentDown = block.getRelative(BlockFace.DOWN);
            if (adjacentDown.getType().equals(block.getType())) {
                searchDoor(adjacentDown, horizontal, vertical);
            } else {
                parseNearbySigns(adjacentDown);
                add(adjacentDown);
            }
        }
    }

    private void searchFenceGate(Block block, boolean horizontal, boolean vertical) {
        if (!add(block)) {
            return;
        }
        for (BlockFace bf : Deadbolt.getConfig().CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            if (horizontal) {
                switch (adjacent.getType()) {
                    case FENCE_GATE:
                    case BIRCH_FENCE_GATE:
                    case ACACIA_FENCE_GATE:
                    case DARK_OAK_FENCE_GATE:
                    case JUNGLE_FENCE_GATE:
                    case SPRUCE_FENCE_GATE:
                        searchFenceGate(adjacent, horizontal, vertical);
                        break;
                }
            } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
                parseSignAttached(adjacent, block);
            } else {
                parseNearbySigns(adjacent);
            }
        }
        if (vertical) {
            for (BlockFace bf : Deadbolt.getConfig().VERTICAL_FACES) {
                Block adjacent = block.getRelative(bf);
                switch (adjacent.getType()) {
                    case FENCE_GATE:
                    case BIRCH_FENCE_GATE:
                    case ACACIA_FENCE_GATE:
                    case DARK_OAK_FENCE_GATE:
                    case JUNGLE_FENCE_GATE:
                    case SPRUCE_FENCE_GATE:
                        searchFenceGate(adjacent, horizontal, vertical);
                        break;
                }
            }
        }
    }

    private void searchTrapDoor(Block block, boolean horizontal, boolean vertical) {
        if (!add(block)) {
            return;
        }
        Block hinge = block.getRelative(((TrapDoor) block.getState().getData()).getAttachedFace());
        parseNearbySigns(hinge);
        add(hinge);
        for (BlockFace bf : Deadbolt.getConfig().CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            if (horizontal && adjacent.getState().getData() instanceof TrapDoor) {
                searchTrapDoor(adjacent, horizontal, vertical);
            } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
                parseSignAttached(adjacent, block);
            }
        }
        if (vertical) {
            for (BlockFace bf : Deadbolt.getConfig().VERTICAL_FACES) {
                Block adjacent = block.getRelative(bf);
                if (adjacent.getState().getData() instanceof TrapDoor) {
                    searchTrapDoor(adjacent, horizontal, vertical);
                } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
                    BlockState state = adjacent.getState();
                    org.bukkit.material.Sign signData = (org.bukkit.material.Sign) state.getData();
                    Block attached = adjacent.getRelative(signData.getAttachedFace());
                    if (parseSign((Sign) state)) {
                        add(adjacent, attached);
                    }
                }
            }
        }
    }

    private void searchSimpleBlock(Block block, boolean horizontal, boolean vertical) {
        if (!add(block)) {
            return;
        }
        for (BlockFace bf : Deadbolt.getConfig().CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            if (horizontal && adjacent.getType().equals(block.getType())) {
                searchSimpleBlock(adjacent, horizontal, vertical);
            } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
                parseSignAttached(adjacent, block);
            }
        }
        if (vertical) {
            for (BlockFace bf : Deadbolt.getConfig().VERTICAL_FACES) {
                Block adjacent = block.getRelative(bf);
                if (adjacent.getType().equals(block.getType())) {
                    searchSimpleBlock(adjacent, horizontal, vertical);
                }
            }
        }
    }

    private void searchFurnace(Block block, boolean horizontal, boolean vertical) {
        if (!add(block)) {
            return;
        }
        for (BlockFace bf : Deadbolt.getConfig().CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            if (horizontal && adjacent.getState() instanceof Furnace) {
                searchFurnace(adjacent, horizontal, vertical);
            } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
                parseSignAttached(adjacent, block);
            }
        }
        if (vertical) {
            for (BlockFace bf : Deadbolt.getConfig().VERTICAL_FACES) {
                Block adjacent = block.getRelative(bf);
                if (adjacent.getState() instanceof Furnace) {
                    searchFurnace(adjacent, horizontal, vertical);
                }
            }
        }
    }

    private void searchChest(Block block, boolean horizontal, boolean vertical) {
        if (!add(block)) {
            return;
        }
        for (BlockFace bf : Deadbolt.getConfig().CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            if (horizontal && adjacent.getState() instanceof Chest) {
                searchChest(adjacent, horizontal, vertical);
            } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
                parseSignAttached(adjacent, block);
            }
        }
        if (vertical) {
            for (BlockFace bf : Deadbolt.getConfig().VERTICAL_FACES) {
                Block adjacent = block.getRelative(bf);
                if (adjacent.getState() instanceof Chest) {
                    searchChest(adjacent, horizontal, vertical);
                }
            }
        }
    }

    private void parseNearbySigns(Block block) {
        for (BlockFace bf : Deadbolt.getConfig().CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            if (adjacent.getType().equals(Material.WALL_SIGN)) {
                parseSignAttached(adjacent, block);
            }
        }
    }

    private void parseSignAttached(Block signBlock, Block attached) {
        Sign sign = (Sign) signBlock.getState();
        Attachable direction = (Attachable) sign.getData();
        // TODO: Check this, is it attached face, or other?
        if (signBlock.getRelative(direction.getAttachedFace()).equals(attached)) {
            if (parseSign(sign)) {
                add(attached, signBlock);
            }
        }
    }

    private boolean parseSign(Sign sign) {
        String ident = Util.getLine(sign, 0);
        if (Deadbolt.getLanguage().isPrivate(ident)) {
            String line1 = Util.getLine(sign, 1);
            owner = line1.isEmpty() ? owner : line1;
            users.add(Util.getLine(sign, 2));
            users.add(Util.getLine(sign, 3));
            return true;
        } else if (Deadbolt.getLanguage().isMoreUsers(ident)) {
            users.add(Util.getLine(sign, 1));
            users.add(Util.getLine(sign, 2));
            users.add(Util.getLine(sign, 3));
            return true;
        }
        return false;
    }

    public boolean isProtected() {
        return owner != null && !owner.isEmpty();
    }

    public boolean isOwner(Player player) {
        return isProtected() && Util.signNameEqualsPlayerName(owner, player.getName());
    }

    public boolean isUser(Player player) {
        if (isOwner(player) || isEveryone()) {
            return true;
        } else {
            for (String user : users) {
                if (Util.signNameEqualsPlayerName(user, player.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isEveryone() {
        for (String line : users) {
            if (Deadbolt.getLanguage().isEveryone(line)) {
                return true;
            }
        }
        return false;
    }

    public int getTimer() {
        for (String line : users) {
            int timer = Deadbolt.getLanguage().getTimer(line);
            if (timer != -1) {
                return timer;
            }
        }
        return -1;
    }

    private boolean add(Block... block) {
        boolean success = true;
        for (Block b : block) {
            success &= blocks.add(b) && traversed.add(b);
        }
        return success;
    }

    public String getOwner() {
        return owner;
    }

    public Set<String> getUsers() {
        return this.users;
    }

    public Set<Block> getBlocks() {
        return blocks;
    }

    public void toggleDoors(Block block) {
        Set<Block> clickedDoor = new HashSet<Block>();
        if (isNaturalOpen(block)) {
            // special case for Trap Doors so multiple sets don't get miss-aligned
            if (block.getType() != Material.TRAP_DOOR)
                clickedDoor.add(block);
            if (isVerticallyJoined(block)) {
                Block b = block;
                while ((b = b.getRelative(BlockFace.UP)).getType().equals(block.getType())
                        // special case for Trap Doors so it works vertically
                        && b.getType() != Material.TRAP_DOOR) {
                    clickedDoor.add(b);
                }
                b = block;
                while ((b = b.getRelative(BlockFace.DOWN)).getType().equals(block.getType())
                        // special case for Trap Doors so it works vertically
                        && b.getType() != Material.TRAP_DOOR) {
                    clickedDoor.add(b);
                }
            }
        }

        List<Block> validToggles = new ArrayList<Block>();
        for (Block b : blocks) {
            if (b.getType().equals(block.getType())) {
                validToggles.add(b);
            }
        }
        validToggles.removeAll(clickedDoor);

        for (Block b : validToggles) {
            if (b.getType().equals(block.getType())) {
                b.setData((byte) (b.getData() ^ 0x4));
            }
        }

        if (!isNaturalSound(block) && Deadbolt.getConfig().silent_door_sounds) {
            block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 10);
        }

        if (Deadbolt.getConfig().deny_timed_doors) {
            return;
        }
        int delay = getTimer();
        if (delay == -1) {
            if (Deadbolt.getConfig().forced_timed_doors) {
                delay = Deadbolt.getConfig().forced_timed_doors_delay;
            } else {
                return;
            }
        }
        validToggles.addAll(clickedDoor);

        boolean runonce = true;
        for (Block bl : validToggles) {
            if (ToggleDoorTask.timedBlocks.add(bl)) {
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ToggleDoorTask(bl,
                        (runonce && Deadbolt.getConfig().timed_door_sounds && (isNaturalSound(bl) || Deadbolt.getConfig().silent_door_sounds))),
                        delay * 20);
                runonce = false;
            } else {
                ToggleDoorTask.timedBlocks.remove(bl);
            }
        }
    }

    private boolean isNaturalOpen(Block block) {
        switch (block.getType()) {
            case WOODEN_DOOR:
            case TRAP_DOOR:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case JUNGLE_DOOR:
            case ACACIA_DOOR:
            case DARK_OAK_DOOR:
                return true;
            default:
                return false;
        }
    }

    private boolean isVerticallyJoined(Block block) {
        switch (block.getType()) {
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case JUNGLE_DOOR:
            case ACACIA_DOOR:
            case DARK_OAK_DOOR:
                return true;
            case TRAP_DOOR:
            case IRON_TRAPDOOR:
                return Deadbolt.getConfig().vertical_trapdoors;
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

    /**
     * The purpose of this is to let protections auto-expire if the owner did
     * not play for the last X days.
     *
     * @param playerToInform
     * @return
     */
    public boolean isAutoExpired(Player playerToInform) {
        // Are we even supposed to use the auto-expire feature?
        // Is the feature perhaps disabled in the configuration?
        if (Deadbolt.getConfig().auto_expire_days <= 0) {
            return false;
        }

        //log("Entered isAutoExpired whith playerToInform = ", playerToInform);

        // Fetch the owner string
        String signPlayerName = this.getOwner();
        //log("signPlayerName is", signPlayerName);

        // That must be a valid player name
        if (!PlayerNameUtil.isValidPlayerName(signPlayerName)) {
            return false;
        }

        // What time is it?
        long now = System.currentTimeMillis();

        // This is an unwanted necessity due to sign lines being one char to short.
        // More than one player name could cover for the auto expire.
        // Find all those valid owners.
        Set<String> allValidOwnerNames = PlayerNameUtil.interpretPlayerNameFromSign(signPlayerName);
        //log("allValidOwnerNames are", allValidOwnerNames);

        // At least one of them needs to have been online recently
        boolean hasExpired = true;
        long daysTillExpire = 0;
        String nameThatCovered = null;
        for (String validOwnerName : allValidOwnerNames) {
            long lastPlayed = PlayerNameUtil.getLastPlayed(validOwnerName);
            long millisSinceLastPlayed = now - lastPlayed;
            long daysSinceLastPlayed = (long) Math.floor(millisSinceLastPlayed / (1000 * 60 * 60 * 24));
            daysTillExpire = Deadbolt.getConfig().auto_expire_days - daysSinceLastPlayed;
            //log(validOwnerName, "lastPlayed", lastPlayed, "millisSinceLastPlayed", millisSinceLastPlayed, "daysSinceLastPlayed", daysSinceLastPlayed, "daysTillExpire", daysTillExpire);
            if (daysTillExpire > 0) {
                nameThatCovered = validOwnerName;
                //log("This name covered for it!", nameThatCovered);
                hasExpired = false;
                break;
            }
        }

        //log("hasExpired is", hasExpired);

        if (hasExpired) {
            if (playerToInform != null && !playerToInform.getName().equalsIgnoreCase(nameThatCovered)) {
                Deadbolt.getConfig().sendMessage(playerToInform, ChatColor.RED, Deadbolt.getLanguage().msg_auto_expire_expired);
            }
        } else {
            if (playerToInform != null && !playerToInform.getName().equalsIgnoreCase(nameThatCovered)) {
                Deadbolt.getConfig().sendMessage(playerToInform, ChatColor.YELLOW, Deadbolt.getLanguage().msg_auto_expire_owner_x_days, nameThatCovered, String.valueOf(daysTillExpire));
            }
        }

        return hasExpired;
    }

    /*public void log(Object... things) {
     StringBuilder ret = new StringBuilder();
     for (Object thing : things) {
     ret.append(thing == null ? "NULL" : thing.toString());
     ret.append(" ");
     }
     Deadbolt.getLogger().info(ret.toString());
     }*/
    public boolean isAutoExpired() {
        return this.isAutoExpired(null);
    }
}
