package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.*;
import com.daemitus.deadbolt.listener.ListenerManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private final DeadboltPlugin plugin = Deadbolt.getPlugin();

    public PlayerListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
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
                    Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().msg_deny_block_perm, against.getType().name());
                    return false;
                }

                BlockFace clickedFace = event.getBlockFace();
                if (!Deadbolt.getConfig().CARDINAL_FACES.contains(clickedFace)) {
                    return false;
                }

                Block signBlock = against.getRelative(clickedFace);
                if (!signBlock.getType().equals(Material.AIR)) {
                    return false;
                }

                Deadbolted db = Deadbolt.get(against);
                if (!ListenerManager.canSignChangeQuick(db, event)) {
                    return false;
                }
                
                // Trigger an on block place event so other plugins can cancel this.
                BlockState replacedBlockState = new CraftBlockState(signBlock);
                BlockPlaceEvent triggeredEvent = new BlockPlaceEvent(signBlock, replacedBlockState, against, event.getItem(), player, true);
                Bukkit.getPluginManager().callEvent(triggeredEvent);
                if (triggeredEvent.isCancelled()) {
                    return false;
                }

                signBlock.setTypeIdAndData(Material.WALL_SIGN.getId(), Util.getByteFromFacing(clickedFace), false);
                Sign signState = (Sign) signBlock.getState();

                if (!db.isProtected()) {
                    signState.setLine(0, Util.formatForSign(Deadbolt.getLanguage().signtext_private));
                    signState.setLine(1, Util.formatForSign(player.getName()));
                } else if (db.isOwner(player)) {
                    signState.setLine(0, Util.formatForSign(Deadbolt.getLanguage().signtext_moreusers));
                } else if (player.hasPermission(Perm.admin_create)) {
                    signState.setLine(0, Util.formatForSign(Deadbolt.getLanguage().signtext_moreusers));
                    Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().msg_admin_sign_placed, db.getOwner());
                } else {
                    Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().msg_deny_sign_quickplace, db.getOwner());
                    signBlock.setType(Material.AIR);
                    return false;
                }

                signState.update(true);
                ItemStack held = player.getItemInHand();
                held.setAmount(held.getAmount() - 1);
                if (held.getAmount() == 0) {
                    player.setItemInHand(null);
                }
                event.setCancelled(true);
                return false;
            default:
                return true;
        }
    }

    private boolean canQuickProtect(Player player, Block block) {
        if (Deadbolt.getConfig().deny_quick_signs) {
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
        Deadbolted db = Deadbolt.get(block);

        if (!db.isProtected()) {
            return true;
        }
        if (db.isAutoExpired(player)) {
            return true;
        }

        if (db.isUser(player) || ListenerManager.canPlayerInteract(db, event)) {
            db.toggleDoors(block);
            return true;
        }

        if (player.hasPermission(Perm.admin_bypass)) {
            db.toggleDoors(block);
            Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().msg_admin_bypass, db.getOwner());
            return true;
        }

        Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().msg_deny_access_door);
        return false;
    }

    private boolean onPlayerInteractContainer(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Deadbolted db = Deadbolt.get(block);

        if (!db.isProtected()) {
            return true;
        }
        if (db.isAutoExpired(player)) {
            return true;
        }

        if (db.isUser(player) || ListenerManager.canPlayerInteract(db, event)) {
            return true;
        }

        if (player.hasPermission(Perm.admin_container)) {
            Deadbolt.getConfig().sendBroadcast(Perm.broadcast_admin_container, ChatColor.RED, Deadbolt.getLanguage().msg_admin_container, player.getName(), db.getOwner());
            return true;
        }

        Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().msg_deny_access_container);
        return false;
    }

    private boolean onPlayerInteractWallSign(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Deadbolted db = Deadbolt.get(block);

        if (!db.isProtected()) {
            return true;
        }
        if (db.isAutoExpired(player)) {
            return true;
        }

        if (db.isOwner(player)) {
            Deadbolt.getConfig().selectedSign.put(player, block);
            Deadbolt.getConfig().sendMessage(player, ChatColor.GOLD, Deadbolt.getLanguage().cmd_sign_selected);
            return false;
        }

        if (player.hasPermission(Perm.admin_commands)) {
            Deadbolt.getConfig().selectedSign.put(player, block);
            Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().msg_admin_sign_selection, db.getOwner());
            return false;
        }

        Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().msg_deny_sign_selection);
        return false;
    }
}
