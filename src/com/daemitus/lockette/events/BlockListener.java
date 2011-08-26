package com.daemitus.lockette.events;

import com.daemitus.lockette.ConfigManager;
import com.daemitus.lockette.Lockette;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.ContainerBlock;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;
import org.bukkit.material.TrapDoor;
import org.bukkit.plugin.PluginManager;

public class BlockListener extends org.bukkit.event.block.BlockListener {

    private final Lockette plugin;

    public BlockListener(final Lockette plugin) {
        this.plugin = plugin;
    }

    public void registerEvents(PluginManager pm) {
        pm.registerEvent(Type.BLOCK_BREAK, this, Priority.Normal, plugin);
        pm.registerEvent(Type.BLOCK_PLACE, this, Priority.Normal, plugin);
        pm.registerEvent(Type.REDSTONE_CHANGE, this, Priority.Normal, plugin);
        pm.registerEvent(Type.SIGN_CHANGE, this, Priority.Normal, plugin);
        pm.registerEvent(Type.BLOCK_PISTON_EXTEND, this, Priority.Normal, plugin);
        pm.registerEvent(Type.BLOCK_PISTON_RETRACT, this, Priority.Normal, plugin);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String owner = plugin.logic.getOwnerName(block);
        if (owner.equals("") || owner.equalsIgnoreCase(plugin.logic.truncate(player.getName())))
            return;
        event.setCancelled(true);
        plugin.sendMessage(player, "msg-deny-block-break", true);
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;
        Block against = event.getBlockAgainst();
        if (against.getType().equals(Material.WALL_SIGN) && plugin.logic.isProtected(against)) {
            event.setCancelled(true);
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();
        BlockState state = block.getState();
        MaterialData data = state.getData();


        if (type.equals(Material.CHEST)) {
            if (!onChestPlace(player, block)) {
                event.setCancelled(true);
                plugin.sendMessage(player, "msg-deny-placement-chest", true);
            }
        } else if (data instanceof Door) {
            if (!onDoorPlace(player, block)) {
                event.setCancelled(true);
                block.setType(Material.SAND);
                block.setType(Material.AIR);
                block.getRelative(BlockFace.UP).setType(Material.SAND);
                block.getRelative(BlockFace.UP).setType(Material.AIR);
                plugin.sendMessage(player, "msg-deny-placement-door", true);
            }
        } else if (data instanceof TrapDoor) {
            if (!onTrapDoorPlace(player, against)) {
                event.setCancelled(true);
                plugin.sendMessage(player, "msg-deny-placement-trapdoor", true);
            }
        }
    }

    private boolean onChestPlace(Player player, Block block) {
        for (BlockFace bf : plugin.logic.horizontalBlockFaces) {
            Block adjacent = block.getRelative(bf);
            if (!adjacent.getType().equals(block.getType()))
                continue;
            String owner = plugin.logic.getOwnerName(adjacent);
            if (owner.equals("") || owner.equals(plugin.logic.truncate(player.getName())))
                continue;
            return false;
        }
        return true;
    }

    private boolean onDoorPlace(Player player, Block block) {
        for (BlockFace bf : plugin.logic.horizontalBlockFaces) {
            Block adjacent = block.getRelative(bf);
            if (!adjacent.getType().equals(block.getType()))
                continue;
            String owner = plugin.logic.getOwnerName(adjacent);
            if (owner.equals("") || owner.equals(plugin.logic.truncate(player.getName())))
                continue;
            return false;
        }
        String owner = plugin.logic.getOwnerName(block.getRelative(0, 2, 0));
        if (owner.equals("") || owner.equals(plugin.logic.truncate(player.getName())))
            return true;
        return false;
    }

    private boolean onTrapDoorPlace(Player player, Block against) {
        String owner = plugin.logic.getOwnerName(against);
        if (owner.equals("") || owner.equals(plugin.logic.truncate(player.getName()))) {
            return true;
        }
        return false;
    }

    //TODO holy shit. optimize this.
    @Override
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String text = plugin.logic.stripColor(event.getLine(0));

        boolean primary = false;
        boolean moreusers = false;
        if (text.equalsIgnoreCase(ConfigManager.getDefault("signtext-private"))
            || text.equalsIgnoreCase(ConfigManager.getLocale("signtext-private")))
            primary = true;
        else if (text.equalsIgnoreCase(ConfigManager.getDefault("signtext-moreusers"))
                 || text.equalsIgnoreCase(ConfigManager.getLocale("signtext-moreusers")))
            moreusers = true;

        if (!primary && !moreusers)
            return;

        boolean valid = false;
        if (block.getType().equals(Material.WALL_SIGN)) {
            Block against = plugin.logic.getBlockSignAttachedTo(block);
            String owner = plugin.logic.getOwnerName(against);
            if (primary) {
                if (!owner.equals("")) {
                    plugin.sendMessage(player, "msg-deny-placement-sign-private-owner", true);
                } else {
                    for (BlockFace bf : plugin.logic.horizontalBlockFaces) {
                        Block adjacent = against.getRelative(bf);
                        BlockState state = adjacent.getState();
                        MaterialData data = state.getData();
                        if (data instanceof TrapDoor) {
                            TrapDoor trapDoor = (TrapDoor) data;
                            if (adjacent.getRelative(trapDoor.getAttachedFace()).equals(against))
                                valid = true;
                        }
                    }
                    if (!valid) {
                        for (BlockFace bf : plugin.logic.verticalBlockFaces) {
                            Block vertical = against.getRelative(bf);
                            BlockState state = vertical.getState();
                            MaterialData data = state.getData();
                            if (data instanceof Door)
                                valid = true;
                        }
                    }
                    if (!valid)
                        plugin.sendMessage(player, "msg-deny-placement-sign-nothing-nearby", true);
                }
            } else if (moreusers) {
                if (owner.equals("")) {
                    plugin.sendMessage(player, "msg-deny-placement-sign-moreusers-needs-private", true);
                } else if (!owner.equalsIgnoreCase(plugin.logic.truncate(player.getName()))) {
                    plugin.sendMessage(player, "msg-deny-placement-sign-moreusers-owner", true);
                } else {
                    valid = true;
                }
            }
        } else {
            block.setType(Material.WALL_SIGN);
            String error = "";
            for (byte i = 2; i < 6 && !valid; i++) {
                block.setData(i);
                Block against = plugin.logic.getBlockSignAttachedTo(block);
                if (against == null)
                    continue;
                String owner = plugin.logic.getOwnerName(against);
                if (primary) {
                    if (!owner.equals("")) {
                        error = "msg-deny-placement-sign-private-owner";
                    } else {
                        if (against.getState() instanceof ContainerBlock) {
                            valid = true;
                        } else if (against.getState().getData() instanceof Door) {
                            valid = true;
                        } else {
                            for (BlockFace bf : plugin.logic.horizontalBlockFaces) {
                                Block adjacent = against.getRelative(bf);
                                BlockState state = adjacent.getState();
                                MaterialData data = state.getData();
                                if (data instanceof TrapDoor) {
                                    TrapDoor trapDoor = (TrapDoor) data;
                                    if (adjacent.getRelative(trapDoor.getAttachedFace()).equals(against))
                                        valid = true;
                                }
                            }
                            if (!valid) {
                                for (BlockFace bf : plugin.logic.verticalBlockFaces) {
                                    Block vertical = against.getRelative(bf);
                                    BlockState state = vertical.getState();
                                    MaterialData data = state.getData();
                                    if (data instanceof Door)
                                        valid = true;
                                }
                            }
                        }
                    }
                } else if (moreusers) {
                    if (owner.equals("")) {
                        error = "msg-deny-placement-sign-moreusers-needs-private";
                        valid = false;
                    } else if (!owner.equalsIgnoreCase(plugin.logic.truncate(player.getName()))) {
                        error = "msg-deny-placement-sign-moreusers-owner";
                        valid = false;
                    } else {
                        if (against.getState() instanceof ContainerBlock) {
                            valid = true;
                        } else if (against.getState().getData() instanceof Door) {
                            valid = true;
                        } else {

                            for (BlockFace bf : plugin.logic.horizontalBlockFaces) {
                                Block adjacent = against.getRelative(bf);
                                BlockState state = adjacent.getState();
                                MaterialData data = state.getData();
                                if (data instanceof TrapDoor) {
                                    TrapDoor trapDoor = (TrapDoor) data;
                                    if (adjacent.getRelative(trapDoor.getAttachedFace()).equals(against))
                                        valid = true;
                                }
                            }
                            if (!valid) {
                                for (BlockFace bf : plugin.logic.verticalBlockFaces) {
                                    Block vertical = against.getRelative(bf);
                                    BlockState state = vertical.getState();
                                    MaterialData data = state.getData();
                                    if (data instanceof Door)
                                        valid = true;
                                }
                            }
                        }
                    }
                }
            }
            if (!valid)
                if (!error.equals(""))
                    plugin.sendMessage(player, error, true);
                else
                    plugin.sendMessage(player, "msg-deny-placement-sign-nothing-nearby", true);
        }

        if (valid) {
            String lines[] = event.getLines();
            if (primary) {
                if (lines[1].equals("")) {
                    lines[1] = plugin.logic.truncate(player.getName());
                } else if (plugin.getServer().getPlayer(lines[1]) == null) {
                    plugin.sendMessage(player, "msg-player-not-found-warning", false);
                }
            }
            BlockState state = block.getState();
            Sign sign = (Sign) state;
            for (int i = 0; i < 4; i++) {
                sign.setLine(i, lines[i]);
                sign.update(true);
            }
        } else {
            event.setCancelled(true);
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.SIGN, 1));
            block.setType(Material.AIR);
        }
    }

    @Override
    public void onBlockRedstoneChange(
            BlockRedstoneEvent event) {

        Block block = event.getBlock();
        BlockState state = block.getState();
        MaterialData data = state.getData();
        if (!(data instanceof Door || data instanceof TrapDoor))
            return;

        List<String> names = plugin.logic.getAllNames(block);
        if (names.isEmpty()
            || names.contains(ConfigManager.getDefault("signtext-everyone"))
            || names.contains(ConfigManager.getLocale("signtext-everyone")))
            return;
        event.setNewCurrent(event.getOldCurrent());
    }

    //TODO
    @Override
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
    }

    //TODO
    @Override
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
    }
}
