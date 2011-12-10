package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Config;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import com.daemitus.deadbolt.Perm;
import com.daemitus.deadbolt.listener.ListenerManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Result;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;

public class PlayerListener extends org.bukkit.event.player.PlayerListener {

    private final Deadbolt plugin;

    public PlayerListener(final Deadbolt plugin, final PluginManager pm) {
        this.plugin = plugin;
        pm.registerEvent(Type.PLAYER_INTERACT, this, Priority.Normal, plugin);
        pm.registerEvent(Type.PLAYER_QUIT, this, Priority.Normal, plugin);
    }

    @Override
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
        if (event.getPlayer().getItemInHand().getType().equals(Material.SIGN))
            return placeQuickSign(event);
        else {
            switch (event.getClickedBlock().getType()) {
                case WOODEN_DOOR:
                case IRON_DOOR_BLOCK:
                case TRAP_DOOR:
                case FENCE_GATE:
                    return onPlayerInteractDoor(event);
                case CHEST:
                case FURNACE:
                case BURNING_FURNACE:
                case DISPENSER:
                    return onPlayerInteractContainer(event);
                case WALL_SIGN:
                    return onPlayerInteractWallSign(event);
                default:
                    return true;
            }
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

                if (!canQuickProtect(player, against)) {
                    Config.sendMessage(player, ChatColor.RED, Config.msg_deny_block_perm, against.getType().name());
                    return false;
                }

                BlockFace clickedFace = event.getBlockFace();
                if (!Config.CARDINAL_FACES.contains(clickedFace))
                    return false;

                Block signBlock = against.getRelative(clickedFace);
                if (!signBlock.getType().equals(Material.AIR))
                    return false;

                Deadbolted db = Deadbolted.get(against);
                if (!ListenerManager.canSignChangeQuick(db, event))
                    return false;

                signBlock.setTypeIdAndData(Material.WALL_SIGN.getId(), Config.getByteFromFacing(clickedFace), false);
                Sign signState = (Sign) signBlock.getState();

                if (!db.isProtected()) {
                    signState.setLine(0, Config.formatForSign(Config.default_colors_private[0] + Config.locale_private));
                    signState.setLine(1, Config.formatForSign(Config.default_colors_private[1] + Config.truncateName(player.getName())));
                } else if (db.isOwner(player)) {
                    signState.setLine(0, Config.formatForSign(Config.default_colors_moreusers[0] + Config.locale_moreusers));
                } else if (Config.hasPermission(player, Perm.admin_create)) {
                    signState.setLine(0, Config.formatForSign(Config.default_colors_moreusers[0] + Config.locale_moreusers));
                    Config.sendMessage(player, ChatColor.RED, Config.msg_admin_sign_placed, db.getOwner());
                } else {
                    Config.sendMessage(player, ChatColor.RED, Config.msg_deny_sign_quickplace, db.getOwner());
                    signBlock.setType(Material.AIR);
                    return false;
                }

                signState.update(true);
                ItemStack held = player.getItemInHand();
                held.setAmount(held.getAmount() - 1);
                if (held.getAmount() == 0)
                    player.setItemInHand(null);
                return false;
            default:
                return true;
        }
    }

    private boolean canQuickProtect(Player player, Block block) {
        if (Config.deny_quick_signs)
            return false;
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
            default:
                return false;
        }
    }

    private boolean onPlayerInteractDoor(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Deadbolted db = Deadbolted.get(block);
        if (!db.isProtected()) {
            return true;
        } else if (db.isUser(player) || ListenerManager.canPlayerInteract(db, event)) {
            db.toggleDoors(block);
            return true;
        } else if (Config.hasPermission(player, Perm.admin_bypass)) {
            db.toggleDoors(block);
            Config.sendMessage(player, ChatColor.RED, Config.msg_admin_bypass, db.getOwner());
            return true;
        } else {
            Config.sendMessage(player, ChatColor.RED, Config.msg_deny_access_door);
            return false;
        }
    }

    private boolean onPlayerInteractContainer(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Deadbolted db = Deadbolted.get(block);
        if (!db.isProtected()) {
            return true;
        } else if (db.isUser(player) || ListenerManager.canPlayerInteract(db, event)) {
            return true;
        } else if (Config.hasPermission(player, Perm.admin_container)) {
            Config.sendBroadcast(Perm.broadcast_admin_container, ChatColor.RED, Config.msg_admin_container, player.getName(), db.getOwner());
            return true;
        } else {
            Config.sendMessage(player, ChatColor.RED, Config.msg_deny_access_container);
            return false;
        }
    }

    private boolean onPlayerInteractWallSign(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Deadbolted db = Deadbolted.get(block);
        if (!db.isProtected()) {
            return true;
        } else if (db.isOwner(player)) {
            Config.selectedSign.put(player, block);
            Config.sendMessage(player, ChatColor.GOLD, Config.cmd_sign_selected);
            return false;
        } else if (Config.hasPermission(player, Perm.admin_commands)) {
            Config.selectedSign.put(player, block);
            Config.sendMessage(player, ChatColor.RED, Config.msg_admin_sign_selection, db.getOwner());
            return false;
        } else {
            Config.sendMessage(player, ChatColor.RED, Config.msg_deny_sign_selection);
            return false;
        }
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        Config.selectedSign.remove(event.getPlayer());
    }
}