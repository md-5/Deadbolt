package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.DeadboltPlugin;
import com.daemitus.deadbolt.Deadbolted;
import com.daemitus.deadbolt.Perm;
import com.daemitus.deadbolt.listener.ListenerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {

    private final DeadboltPlugin plugin = Deadbolt.getPlugin();

    public BlockListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        Deadbolted db = Deadbolt.get(block);

        if (db.isProtected() && !db.isAutoExpired() && !db.isOwner(player) && !ListenerManager.canBlockBreak(db, event)) {
            if (player.hasPermission(Perm.admin_break)) {
                Deadbolt.getConfig().sendBroadcast(Perm.admin_broadcast_break, ChatColor.RED, Deadbolt.getLanguage().msg_admin_break, player.getName(), db.getOwner());
            } else {
                Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().msg_deny_block_break);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        Player player = event.getPlayer();
        Block against = event.getBlockAgainst();

        if (against.getType().equals(Material.WALL_SIGN) && Deadbolt.getLanguage().isValidWallSign((Sign) against.getState())) {
            event.setCancelled(true);
            return;
        }

        Deadbolted db = Deadbolt.get(block);
        switch (block.getType()) {
            case CHEST:
            case FURNACE:
            case CAULDRON:
            case DISPENSER:
            case BREWING_STAND:
            case BURNING_FURNACE:
            case ENCHANTMENT_TABLE:
                if (player.hasPermission(getPermission(block.getType())) && Deadbolt.getConfig().reminder.add(player)) {
                    Deadbolt.getConfig().sendMessage(player, ChatColor.GOLD, Deadbolt.getLanguage().msg_reminder_lock_your_chests);
                }
                if (db.isProtected() && !db.isOwner(player)) {
                    event.setCancelled(true);
                    Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().msg_deny_container_expansion);
                }
                return;
            case IRON_DOOR_BLOCK:
            case WOODEN_DOOR:
                if (player.hasPermission(getPermission(block.getType())) && Deadbolt.getConfig().reminder.add(player)) {
                    Deadbolt.getConfig().sendMessage(player, ChatColor.GOLD, Deadbolt.getLanguage().msg_reminder_lock_your_chests);
                }
                if (db.isProtected() && !db.isOwner(player)) {
                    Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().msg_deny_door_expansion);
                    Block upBlock = block.getRelative(BlockFace.UP);
                    block.setType(Material.STONE);
                    block.setType(Material.AIR);
                    upBlock.setType(Material.STONE);
                    upBlock.setType(Material.AIR);
                    event.setCancelled(true);
                }
                return;
            case TRAP_DOOR:
                if (player.hasPermission(getPermission(block.getType())) && Deadbolt.getConfig().reminder.add(player)) {
                    Deadbolt.getConfig().sendMessage(player, ChatColor.GOLD, Deadbolt.getLanguage().msg_reminder_lock_your_chests);
                }
                if (db.isProtected() && !db.isOwner(player)) {
                    Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().msg_deny_trapdoor_expansion);
                    event.setCancelled(true);
                }
                return;
            case FENCE_GATE:
                if (player.hasPermission(getPermission(block.getType())) && Deadbolt.getConfig().reminder.add(player)) {
                    Deadbolt.getConfig().sendMessage(player, ChatColor.GOLD, Deadbolt.getLanguage().msg_reminder_lock_your_chests);
                }
                if (db.isProtected() && !db.isOwner(player)) {
                    Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().msg_deny_fencegate_expansion);
                    event.setCancelled(true);
                }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        Deadbolted db = Deadbolt.get(block);
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
