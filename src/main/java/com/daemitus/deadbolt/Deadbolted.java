package com.daemitus.deadbolt;

import com.daemitus.deadbolt.tasks.ToggleDoorTask;
import com.daemitus.deadbolt.util.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.material.Door;
import org.bukkit.material.TrapDoor;

public final class Deadbolted {

    private Set<Block> blocks = new HashSet<Block>();
    private Set<Block> traversed = new HashSet<Block>();
    private String owner = null;
    private Set<String> users = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    private static Deadbolt plugin;

    public Deadbolted(final Deadbolt plugin) {
        Deadbolted.plugin = plugin;
    }

    public static Deadbolted get(Block block) {
        return new Deadbolted(block);
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
                if (plugin.config.isValidWallSign(signState)) {
                    search(Util.getSignAttached(signState));
                }
                break;
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
                searchDoor(block, true, true);
                break;
            case FENCE_GATE:
                searchFenceGate(block, true, true);
                break;
            case TRAP_DOOR:
                searchTrapDoor(block, true, plugin.config.vertical_trapdoors);
                break;
            case DISPENSER:
                searchSimpleBlock(block, plugin.config.group_dispensers, plugin.config.group_dispensers);
                break;
            case BREWING_STAND:
                searchSimpleBlock(block, plugin.config.group_brewing_stands, plugin.config.group_brewing_stands);
                break;
            case ENCHANTMENT_TABLE:
                searchSimpleBlock(block, plugin.config.group_enchantment_tables, plugin.config.group_enchantment_tables);
                break;
            case CAULDRON:
                searchSimpleBlock(block, plugin.config.group_cauldrons, plugin.config.group_cauldrons);
                break;
            case FURNACE:
            case BURNING_FURNACE:
                searchFurnace(block, plugin.config.group_furnaces, plugin.config.group_furnaces);
                break;
            case CHEST:
                searchChest(block, true, false);
                break;
            default:
                for (BlockFace bf : plugin.config.CARDINAL_FACES) {
                    Block adjacent = block.getRelative(bf);
                    if (adjacent.getState().getData() instanceof TrapDoor) {
                        Block hinge = adjacent.getRelative(((TrapDoor) adjacent.getState().getData()).getAttachedFace());
                        if (hinge.equals(block)) {
                            search(adjacent);
                        }
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
        if (!add(block)) {
            return;
        }
        for (BlockFace bf : plugin.config.CARDINAL_FACES) {
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
        for (BlockFace bf : plugin.config.CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            if (horizontal && adjacent.getType().equals(Material.FENCE_GATE)) {
                searchFenceGate(adjacent, horizontal, vertical);
            } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
                parseSignAttached(adjacent, block);
            } else {
                parseNearbySigns(adjacent);
            }
        }
        if (vertical) {
            for (BlockFace bf : plugin.config.VERTICAL_FACES) {
                Block adjacent = block.getRelative(bf);
                if (adjacent.getType().equals(Material.FENCE_GATE)) {
                    searchFenceGate(adjacent, horizontal, vertical);
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
        for (BlockFace bf : plugin.config.CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            if (horizontal && adjacent.getState().getData() instanceof TrapDoor) {
                searchTrapDoor(adjacent, horizontal, vertical);
            } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
                parseSignAttached(adjacent, block);
            }
        }
        if (vertical) {
            for (BlockFace bf : plugin.config.VERTICAL_FACES) {
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
        for (BlockFace bf : plugin.config.CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            if (horizontal && adjacent.getType().equals(block.getType())) {
                searchSimpleBlock(adjacent, horizontal, vertical);
            } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
                parseSignAttached(adjacent, block);
            }
        }
        if (vertical) {
            for (BlockFace bf : plugin.config.VERTICAL_FACES) {
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
        for (BlockFace bf : plugin.config.CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            if (horizontal && adjacent.getState() instanceof Furnace) {
                searchFurnace(adjacent, horizontal, vertical);
            } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
                parseSignAttached(adjacent, block);
            }
        }
        if (vertical) {
            for (BlockFace bf : plugin.config.VERTICAL_FACES) {
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
        for (BlockFace bf : plugin.config.CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            if (horizontal && adjacent.getState() instanceof Chest) {
                searchChest(adjacent, horizontal, vertical);
            } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
                parseSignAttached(adjacent, block);
            }
        }
        if (vertical) {
            for (BlockFace bf : plugin.config.VERTICAL_FACES) {
                Block adjacent = block.getRelative(bf);
                if (adjacent.getState() instanceof Chest) {
                    searchChest(adjacent, horizontal, vertical);
                }
            }
        }
    }

    private void parseNearbySigns(Block block) {
        for (BlockFace bf : plugin.config.CARDINAL_FACES) {
            Block adjacent = block.getRelative(bf);
            if (adjacent.getType().equals(Material.WALL_SIGN)) {
                parseSignAttached(adjacent, block);
            }
        }
    }

    private void parseSignAttached(Block signBlock, Block attached) {
        if (signBlock.getRelative(Util.getFacingFromByte(signBlock.getData()).getOppositeFace()).equals(attached)) {
            if (parseSign((Sign) signBlock.getState())) {
                add(attached, signBlock);
            }
        }
    }

    private boolean parseSign(Sign sign) {
        String ident = Util.getLine(sign, 0);
        if (plugin.config.isPrivate(ident)) {
            String line1 = Util.getLine(sign, 1);
            owner = line1.isEmpty() ? owner : line1;
            users.add(Util.getLine(sign, 2));
            users.add(Util.getLine(sign, 3));
            return true;
        } else if (plugin.config.isMoreUsers(ident)) {
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
        return Util.truncateName(owner).equalsIgnoreCase(Util.truncateName(player.getName()));
    }

    public boolean isUser(Player player) {
        if (isOwner(player) || isEveryone()) {
            return true;
        } else {
            String name = Util.truncateName(player.getName());
            for (String user : users) {
                if (Util.truncateName(user).equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isEveryone() {
        for (String line : users) {
            if (plugin.config.isEveryone(line)) {
                return true;
            }
        }
        return false;
    }

    public int getTimer() {
        for (String line : users) {
            int timer = plugin.config.getTimer(line);
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
            clickedDoor.add(block);
            if (isVerticallyJoined(block)) {
                Block b = block;
                while ((b = b.getRelative(BlockFace.UP)).getType().equals(block.getType())) {
                    clickedDoor.add(b);
                }
                b = block;
                while ((b = b.getRelative(BlockFace.DOWN)).getType().equals(block.getType())) {
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

        if (!isNaturalSound(block) && plugin.config.silent_door_sounds) {
            block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 10);
        }

        if (plugin.config.deny_timed_doors) {
            return;
        }
        int delay = getTimer();
        if (delay == -1) {
            if (plugin.config.forced_timed_doors) {
                delay = plugin.config.forced_timed_doors_delay;
            } else {
                return;
            }
        }
        validToggles.addAll(clickedDoor);

        boolean runonce = true;
        for (Block bl : validToggles) {
            if (ToggleDoorTask.timedBlocks.add(bl)) {
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ToggleDoorTask(bl,
                        (runonce && plugin.config.timed_door_sounds && (isNaturalSound(bl) ? true : plugin.config.silent_door_sounds))),
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
                return plugin.config.vertical_trapdoors;
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
     */
    public boolean isAutoExpired(Player playerToInform) {
        // Are we even supposed to use the auto-expire feature?
        // Is the feature perhaps disabled in the configuration?
        if (Deadbolt.instance.config.auto_expire_days <= 0) {
            return false;
        }

        // Fetch the owner string
        String ownerString = this.getOwner();

        // That must be a valid player name
        if (!Pattern.matches("^[a-zA-Z0-9_]{2,16}$", ownerString)) {
            return false;
        }

        // So when did the player last play? Has it expired yet?
        long lastPlayed = 0;
        Player player = Bukkit.getPlayerExact(ownerString);
        if (player != null && player.isOnline()) {
            lastPlayed = System.currentTimeMillis();
        } else {
            OfflinePlayer offlineOwner = Bukkit.getOfflinePlayer(ownerString);
            lastPlayed = offlineOwner.getLastPlayed();
        }
        long millisSinceLastPlayed = System.currentTimeMillis() - lastPlayed;
        long daysSinceLastPlayed = (long) Math.floor(millisSinceLastPlayed / (1000 * 60 * 60 * 24));
        long daysTillExpire = Deadbolt.instance.config.auto_expire_days - daysSinceLastPlayed;
        boolean expired = (daysTillExpire <= 0);

        if (expired) {
            if (playerToInform != null && !playerToInform.getName().equalsIgnoreCase(ownerString)) {
                Deadbolt.instance.config.sendMessage(playerToInform, ChatColor.RED, Deadbolt.instance.config.msg_auto_expire_expired);
            }
            return true;
        } else {
            if (playerToInform != null && !playerToInform.getName().equalsIgnoreCase(ownerString)) {
                Deadbolt.instance.config.sendMessage(playerToInform, ChatColor.YELLOW, Deadbolt.instance.config.msg_auto_expire_owner_x_days, ownerString, String.valueOf(daysTillExpire));
            }
            return false;
        }
    }

    public boolean isAutoExpired() {
        return this.isAutoExpired(null);
    }
}
