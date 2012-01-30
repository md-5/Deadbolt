package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Perm;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import com.daemitus.deadbolt.listener.ListenerManager;
import com.daemitus.deadbolt.tasks.SignUpdateTask;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public final class BlockListener implements Listener {

    private final Deadbolt plugin = Deadbolt.instance;

    public BlockListener() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        if (block == null) {
            return;
        }
        Player player = event.getPlayer();
        Deadbolted db = Deadbolted.get(block);
        if (!db.isProtected()) {
            return;
        }
        if (db.isOwner(player)) {
            return;
        }
        if (ListenerManager.canBlockBreak(db, event)) {
            return;
        }
        if (plugin.config.hasPermission(player, Perm.admin_break)) {
            plugin.config.sendBroadcast(Perm.admin_broadcast_break, ChatColor.RED, plugin.config.msg_admin_break, player.getName(), db.getOwner());
            Deadbolt.logger.log(Level.INFO, String.format("[Deadbolt] " + plugin.config.msg_admin_break, player.getName(), db.getOwner()));
            return;
        }

        event.setCancelled(true);
        plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_block_break);
        if (block.getType().equals(Material.WALL_SIGN)) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SignUpdateTask(block), 1);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlockPlaced();
        if (block == null) {
            return;
        }
        Player player = event.getPlayer();
        Block against = event.getBlockAgainst();

        if (against.getType().equals(Material.WALL_SIGN) && plugin.config.isValidWallSign((Sign) against.getState())) {
            event.setCancelled(true);
            return;
        }

        Deadbolted db = Deadbolted.get(block);
        switch (block.getType()) {
            case CHEST:
            case FURNACE:
            case CAULDRON:
            case DISPENSER:
            case BREWING_STAND:
            case BURNING_FURNACE:
            case ENCHANTMENT_TABLE:
                if (plugin.config.hasPermission(player, getPermission(block.getType())) && plugin.config.reminder.add(player)) {
                    plugin.config.sendMessage(player, ChatColor.GOLD, plugin.config.msg_reminder_lock_your_chests);
                }
                if (db.isProtected() && !db.isOwner(player)) {
                    event.setCancelled(true);
                    plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_container_expansion);
                }
                return;
            case IRON_DOOR_BLOCK:
            case WOODEN_DOOR:
                if (plugin.config.hasPermission(player, getPermission(block.getType())) && plugin.config.reminder.add(player)) {
                    plugin.config.sendMessage(player, ChatColor.GOLD, plugin.config.msg_reminder_lock_your_chests);
                }
                if (db.isProtected() && !db.isOwner(player)) {
                    plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_door_expansion);
                    Block upBlock = block.getRelative(BlockFace.UP);
                    block.setType(Material.STONE);
                    block.setType(Material.AIR);
                    upBlock.setType(Material.STONE);
                    upBlock.setType(Material.AIR);
                    event.setCancelled(true);
                }
                return;
            case TRAP_DOOR:
                if (plugin.config.hasPermission(player, getPermission(block.getType())) && plugin.config.reminder.add(player)) {
                    plugin.config.sendMessage(player, ChatColor.GOLD, plugin.config.msg_reminder_lock_your_chests);
                }
                if (db.isProtected() && !db.isOwner(player)) {
                    plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_trapdoor_expansion);
                    event.setCancelled(true);
                }
                return;
            case FENCE_GATE:
                if (plugin.config.hasPermission(player, getPermission(block.getType())) && plugin.config.reminder.add(player)) {
                    plugin.config.sendMessage(player, ChatColor.GOLD, plugin.config.msg_reminder_lock_your_chests);
                }
                if (db.isProtected() && !db.isOwner(player)) {
                    plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_fencegate_expansion);
                    event.setCancelled(true);
                }
                return;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBurn(BlockBurnEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Block block = event.getBlock();
        Deadbolted db = Deadbolted.get(block);
        if (db.isProtected() && !ListenerManager.canBlockBurn(db, event)) {
            event.setCancelled(true);
        }
    }

    private String getPermission(Material type) {
        switch (type) {
            case CHEST:
                return Perm.user_create_chest;
            case FURNACE:
            case BURNING_FURNACE:
                return Perm.user_create_furnace;
            case CAULDRON:
                return Perm.user_create_cauldron;
            case DISPENSER:
                return Perm.user_create_dispenser;
            case BREWING_STAND:
                return Perm.user_create_brewery;
            case ENCHANTMENT_TABLE:
                return Perm.user_create_enchant;
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
                return Perm.user_create_door;
            case TRAP_DOOR:
                return Perm.user_create_trapdoor;
            case FENCE_GATE:
                return Perm.user_create_fencegate;
            default:
                return null;
        }
    }
}