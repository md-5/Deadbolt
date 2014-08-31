package com.daemitus.deadbolt;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.material.Attachable;
import org.bukkit.material.Door;
import org.bukkit.material.TrapDoor;

public class Deadbolted {

    private Set<Block> blocks = new HashSet<Block>();
    private Set<Block> traversed = new HashSet<Block>();
    private UUID owner = null;
    private Set<UUID> users = new HashSet<UUID>();
    private boolean everyone;
    private int timer = -1; // Keep order of the UUIDs
    public static DeadboltPlugin plugin;
    private Player player;

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
                searchDoor(block, true, true);
                break;
            case FENCE_GATE:
                searchFenceGate(block, true, true);
                break;
            case TRAP_DOOR:
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
            if (horizontal && adjacent.getType().equals(Material.FENCE_GATE)) {
                searchFenceGate(adjacent, horizontal, vertical);
            } else if (adjacent.getType().equals(Material.WALL_SIGN)) {
                parseSignAttached(adjacent, block);
            } else {
                parseNearbySigns(adjacent);
            }
        }
        if (vertical) {
            for (BlockFace bf : Deadbolt.getConfig().VERTICAL_FACES) {
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
            if (UUIDs.isName(line1)) {
                // Older sign, convert this
                owner = UUIDs.convertName(PlayerNameUtil.interpretSignName(line1));
                sign.setLine(1, UUIDs.format(owner));
                sign.update();
            } else if (UUIDs.validate(line1)) {
                owner = UUIDs.get(line1);
            } else {
                // What is this?
            }

            addUser(sign, 2);
            addUser(sign, 3);
            return true;
        } else if (Deadbolt.getLanguage().isMoreUsers(ident)) {
            addUser(sign, 1);
            addUser(sign, 2);
            addUser(sign, 3);
            return true;
        }


        return false;
    }

    private void addUser(Sign sign, int i) {
        String line = Util.getLine(sign, i);
        if (Deadbolt.getLanguage().isEveryone(line)) {
            this.everyone = true;
        } else {
            int timer = Deadbolt.getLanguage().getTimer(line);
            if (timer != -1) {
                this.timer = timer;
            } else {
                if (UUIDs.isName(line)) {
                    // Convert older signs
                    UUID uuid = UUIDs.convertName(PlayerNameUtil.interpretSignName(line));
                    users.add(uuid);
                    sign.setLine(i, UUIDs.format(uuid));
                } else if (UUIDs.validate(line)) {
                    users.add(UUIDs.get(line));
                } else {
                    // What is this?
                }
            }
        }
    }

    public boolean isProtected() {
        return owner != null;
    }

    public boolean isOwner(Player player) {
        return isProtected() && owner.equals(player.getUniqueId());
    }

    public boolean isUser(Player player) {
        if (isOwner(player) || isEveryone()) {
            return true;
        } else {
            for (UUID user : users) {
                if (user.equals(player.getUniqueId()))
                    return true;
            }
        }
        return false;
    }

    public boolean isEveryone() {
        return everyone;
    }

    public int getTimer() {
        return timer;
    }

    private boolean add(Block... block) {
        boolean success = true;
        for (Block b : block) {
            success &= blocks.add(b) && traversed.add(b);
        }
        return success;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getOwnerName() {
        return isProtected() ? "" : UUIDs.getPlayer(owner).getName();
    }

    public Set<UUID> getUsers() {
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
     * @param playerToInform The player to inform if the protection has expired.
     * @return If this protection has expired.
     */
    public boolean isAutoExpired(Player playerToInform) {
        // Are we even supposed to use the auto-expire feature?
        // Is the feature perhaps disabled in the configuration?
        if (Deadbolt.getConfig().auto_expire_days <= 0) {
            return false;
        }

        // Fetch the owner string
        UUID owner = this.getOwner();

        OfflinePlayer player = UUIDs.getPlayer(owner);

        boolean expired;
        long daysTillExpire;

        // Maybe the player is online at the moment?
        if (player == null) {
            // Who is the owner of the protection?
            expired = true;
            daysTillExpire = 0;
        } else if (player.isOnline()) {
            daysTillExpire = Deadbolt.getConfig().auto_expire_days;
            expired = false; // Player is online now, so it isn't expired
        } else {
            // TODO: Was getLastPlayed() fixed since the creation of PlayerNameUtil?
            long lastPlayed = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - player.getLastPlayed());
            daysTillExpire = Deadbolt.getConfig().auto_expire_days - lastPlayed;
            expired = daysTillExpire <= 0;
        }

        if (expired) {
            if (playerToInform != null && !isOwner(playerToInform)) {
                Deadbolt.getConfig().sendMessage(playerToInform, ChatColor.RED, Deadbolt.getLanguage().msg_auto_expire_expired);
            }
        } else {
            if (playerToInform != null && !isOwner(playerToInform)) {
                Deadbolt.getConfig().sendMessage(playerToInform, ChatColor.YELLOW, Deadbolt.getLanguage().msg_auto_expire_owner_x_days, player.getName(), String.valueOf(daysTillExpire));
            }
        }

        return expired;
    }

    public boolean isAutoExpired() {
        return this.isAutoExpired(null);
    }
}
