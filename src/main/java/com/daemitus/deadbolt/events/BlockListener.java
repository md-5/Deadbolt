package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Conf;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.DeadboltGroup;
import com.daemitus.deadbolt.Perm;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

public class BlockListener extends org.bukkit.event.block.BlockListener {

    private final Deadbolt plugin;

    public BlockListener(final Deadbolt plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvent(Type.BLOCK_BREAK, this, Priority.Low, plugin);
        Bukkit.getPluginManager().registerEvent(Type.BLOCK_PLACE, this, Priority.Normal, plugin);
        Bukkit.getPluginManager().registerEvent(Type.REDSTONE_CHANGE, this, Priority.Low, plugin);
        Bukkit.getPluginManager().registerEvent(Type.BLOCK_PISTON_EXTEND, this, Priority.Low, plugin);
        Bukkit.getPluginManager().registerEvent(Type.BLOCK_PISTON_RETRACT, this, Priority.Low, plugin);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        Player player = event.getPlayer();
        Block block = event.getBlock();
        DeadboltGroup dbg = DeadboltGroup.getRelated(block);
        String owner = dbg.getOwner();
        if (dbg.isOwnerOrNull(player))
            return;
        if (player.hasPermission(Perm.admin_break)) {
            Conf.sendBroadcast(Perm.admin_broadcast_break,
                    String.format(Conf.msg_admin_break, player.getName(), owner),
                    ChatColor.RED);
            Bukkit.getLogger().log(Level.INFO, String.format("Deadbolt: " + Conf.msg_admin_break, player.getName(), owner));
            return;
        }
        event.setCancelled(true);
        if (block.getType().equals(Material.WALL_SIGN))
            ((Sign) block.getState()).update(true);
        Conf.sendMessage(player, Conf.msg_deny_block_break, ChatColor.RED);
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Block against = event.getBlockAgainst();
        if (DeadboltGroup.isValidWallSign(against)) {
            event.setCancelled(true);
            return;
        }

        if (player.hasPermission(Perm.admin_create))
            return;

        DeadboltGroup dbg = DeadboltGroup.getRelated(block);
        switch (block.getType()) {
            case FURNACE:
            case BURNING_FURNACE:
            case DISPENSER:
            case CHEST:
                if (!dbg.isOwnerOrNull(player)) {
                    event.setCancelled(true);
                    Conf.sendMessage(player, Conf.msg_deny_container_expansion, ChatColor.RED);
                } else if (Conf.reminder.add(player)) {
                    Conf.sendMessage(player, Conf.msg_reminder_lock_your_chests, ChatColor.GOLD);
                }
                return;
            case IRON_DOOR_BLOCK:
            case WOODEN_DOOR:
                if (!dbg.isOwnerOrNull(player)) {
                    event.setCancelled(true);
                    Conf.sendMessage(player, Conf.msg_deny_door_expansion, ChatColor.RED);
                    Block upBlock = block.getRelative(BlockFace.UP);
                    if (upBlock.getType().equals(block.getType())) {
                        upBlock.setType(Material.SAND);
                        upBlock.setType(Material.AIR);
                    }
                }
                return;
            case TRAP_DOOR:
                if (!dbg.isOwnerOrNull(player)) {
                    event.setCancelled(true);
                    Conf.sendMessage(player, Conf.msg_deny_trapdoor_placement, ChatColor.RED);
                }
                return;
            case FENCE_GATE:
                if (!dbg.isOwnerOrNull(player)) {
                    event.setCancelled(true);
                    Conf.sendMessage(player, Conf.msg_deny_fencegate_placement, ChatColor.RED);
                }
        }
    }

    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        switch (block.getType()) {
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
            case TRAP_DOOR:
                break;
            default:
                return;
        }
        if (!Conf.redstoneProtection)
            return;

        DeadboltGroup dbg = DeadboltGroup.getRelated(block);
        if (dbg.getOwner() == null)
            return;
        if (!dbg.isAuthorized(null))
            event.setNewCurrent(event.getOldCurrent());
    }

    @Override
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (event.isCancelled())
            return;
        if (!Conf.pistonProtection)
            return;

        Block piston = event.getBlock();
        BlockFace facing = event.getDirection();
        for (int i = 1; i <= event.getLength() + 1; i++) {
            Block check = piston.getRelative(facing.getModX() * i, facing.getModY() * i, facing.getModZ() * i);
            if (DeadboltGroup.getRelated(check).getOwner() != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Override
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (event.isCancelled())
            return;
        if (!Conf.pistonProtection)
            return;
        Block block = event.getBlock();
        if (!block.getType().equals(Material.PISTON_STICKY_BASE))
            return;
        BlockFace facing = event.getDirection();
        Block extension = block.getRelative(facing);
        Block check = extension.getRelative(facing);
        if (DeadboltGroup.getRelated(check).getOwner() != null) {
            event.setCancelled(true);
            extension.setType(Material.AIR);
            block.setData((byte) (block.getData() ^ 0x8));
        }
    }
}