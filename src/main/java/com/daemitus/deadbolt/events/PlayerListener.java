package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import com.daemitus.deadbolt.Perm;
import com.daemitus.deadbolt.listener.ListenerManager;
import com.daemitus.deadbolt.util.Util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public final class PlayerListener implements Listener {

    private final Deadbolt plugin = Deadbolt.instance;

    public PlayerListener() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)
                && !handleLeftClick(event)) {
            event.setUseInteractedBlock(Result.DENY);
            event.setUseItemInHand(Result.DENY);
        }
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                && !handleRightClick(event)) {
            event.setUseInteractedBlock(Result.DENY);
            event.setUseItemInHand(Result.DENY);
        }
    }

    private boolean handleLeftClick(PlayerInteractEvent event) {
        switch (event.getClickedBlock().getType()) {
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
            case TRAP_DOOR:
            case FENCE_GATE:
                return onPlayerInteractDoor(event);
            default:
                return true;
        }
    }

    private boolean handleRightClick(PlayerInteractEvent event) {
        if (event.getPlayer().getItemInHand().getType().equals(Material.SIGN) && !event.isCancelled()) {
            placeQuickSign(event);
        }
        switch (event.getClickedBlock().getType()) {
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
            case TRAP_DOOR:
            case FENCE_GATE:
                return onPlayerInteractDoor(event);
            case CHEST:
            case FURNACE:
            case CAULDRON:
            case DISPENSER:
            case BREWING_STAND:
            case BURNING_FURNACE:
            case ENCHANTMENT_TABLE:
                return onPlayerInteractContainer(event);
            case WALL_SIGN:
                return onPlayerInteractWallSign(event);
            default:
                return true;
        }

    }

    private boolean placeQuickSign(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block against = event.getClickedBlock();

        switch (against.getType()) {
            case CHEST:
            case DISPENSER:
            case FURNACE:
            case BURNING_FURNACE:
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
            case TRAP_DOOR:
            case FENCE_GATE:
            case BREWING_STAND:
            case ENCHANTMENT_TABLE:
            case CAULDRON:

                if (!canQuickProtect(player, against)) {
                    plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_block_perm, against.getType().name());
                    return false;
                }

                BlockFace clickedFace = event.getBlockFace();
                if (!plugin.config.CARDINAL_FACES.contains(clickedFace)) {
                    return false;
                }

                Block signBlock = against.getRelative(clickedFace);
                if (!signBlock.getType().equals(Material.AIR)) {
                    return false;
                }

                Deadbolted db = Deadbolted.get(against);
                if (!ListenerManager.canSignChangeQuick(db, event)) {
                    return false;
                }

                signBlock.setTypeIdAndData(Material.WALL_SIGN.getId(), Util.getByteFromFacing(clickedFace), false);
                Sign signState = (Sign) signBlock.getState();

                if (!db.isProtected()) {
                    signState.setLine(0, Util.formatForSign(plugin.config.default_colors_private[0] + plugin.config.locale_private));
                    signState.setLine(1, Util.formatForSign(plugin.config.default_colors_private[1] + Util.truncateName(player.getName())));
                } else if (db.isOwner(player)) {
                    signState.setLine(0, Util.formatForSign(plugin.config.default_colors_moreusers[0] + plugin.config.locale_moreusers));
                } else if (plugin.config.hasPermission(player, Perm.admin_create)) {
                    signState.setLine(0, Util.formatForSign(plugin.config.default_colors_moreusers[0] + plugin.config.locale_moreusers));
                    plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_admin_sign_placed, db.getOwner());
                } else {
                    plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_sign_quickplace, db.getOwner());
                    signBlock.setType(Material.AIR);
                    return false;
                }

                signState.update(true);
                ItemStack held = player.getItemInHand();
                held.setAmount(held.getAmount() - 1);
                if (held.getAmount() == 0) {
                    player.setItemInHand(null);
                }
                return false;
            default:
                return true;
        }
    }

    private boolean canQuickProtect(Player player, Block block) {
        if (plugin.config.deny_quick_signs) {
            return false;
        }
        switch (block.getType()) {
            case CHEST:
                return player.hasPermission(Perm.user_create_chest);
            case DISPENSER:
                return player.hasPermission(Perm.user_create_dispenser);
            case FURNACE:
                return player.hasPermission(Perm.user_create_furnace);
            case BURNING_FURNACE:
                return player.hasPermission(Perm.user_create_furnace);
            case WOODEN_DOOR:
                return player.hasPermission(Perm.user_create_door);
            case IRON_DOOR_BLOCK:
                return player.hasPermission(Perm.user_create_door);
            case TRAP_DOOR:
                return player.hasPermission(Perm.user_create_trapdoor);
            case FENCE_GATE:
                return player.hasPermission(Perm.user_create_fencegate);
            case BREWING_STAND:
                return player.hasPermission(Perm.user_create_brewery);
            case ENCHANTMENT_TABLE:
                return player.hasPermission(Perm.user_create_enchant);
            case CAULDRON:
                return player.hasPermission(Perm.user_create_cauldron);
            default:
                return false;
        }
    }

    private boolean onPlayerInteractDoor(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Deadbolted db = Deadbolted.get(block);
        
        if (!db.isProtected()) return true;
        if (db.isAutoExpired(player)) return true;
        
        if (db.isUser(player) || ListenerManager.canPlayerInteract(db, event)) {
            db.toggleDoors(block);
            return true;
        }
        
        if (plugin.config.hasPermission(player, Perm.admin_bypass)) {
            db.toggleDoors(block);
            plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_admin_bypass, db.getOwner());
            return true;
        }
        
        plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_access_door);
        return false;
    }

    private boolean onPlayerInteractContainer(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Deadbolted db = Deadbolted.get(block);
        
        if (!db.isProtected()) return true;
        if (db.isAutoExpired(player)) return true;
        
        if (db.isUser(player) || ListenerManager.canPlayerInteract(db, event)) {
            return true;
        }
        
        if (plugin.config.hasPermission(player, Perm.admin_container)) {
            plugin.config.sendBroadcast(Perm.broadcast_admin_container, ChatColor.RED, plugin.config.msg_admin_container, player.getName(), db.getOwner());
            return true;
        }
        
        plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_access_container);
        return false;
    }

    private boolean onPlayerInteractWallSign(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Deadbolted db = Deadbolted.get(block);
        
        if (!db.isProtected()) return true;
        if (db.isAutoExpired(player)) return true;
        
        if (db.isOwner(player)) {
            plugin.config.selectedSign.put(player, block);
            plugin.config.sendMessage(player, ChatColor.GOLD, plugin.config.cmd_sign_selected);
            return false;
        }
        
        if (plugin.config.hasPermission(player, Perm.admin_commands)) {
            plugin.config.selectedSign.put(player, block);
            plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_admin_sign_selection, db.getOwner());
            return false;
        }
    
        plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_sign_selection);
        return false;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.config.selectedSign.remove(event.getPlayer());
    }
}