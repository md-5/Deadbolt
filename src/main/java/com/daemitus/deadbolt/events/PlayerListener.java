package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Conf;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.DeadboltGroup;
import com.daemitus.deadbolt.Perm;
import org.bukkit.Bukkit;
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

public class PlayerListener extends org.bukkit.event.player.PlayerListener {

    private final Deadbolt plugin;

    public PlayerListener(final Deadbolt plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvent(Type.PLAYER_INTERACT, this, Priority.Normal, plugin);
        Bukkit.getPluginManager().registerEvent(Type.PLAYER_QUIT, this, Priority.Normal, plugin);
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            if (!handleLeftClick(event)) {
                event.setUseInteractedBlock(Result.DENY);
                event.setUseItemInHand(Result.DENY);
            }
            return;
        }

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (!handleRightClick(event)) {
                event.setUseInteractedBlock(Result.DENY);
                event.setUseItemInHand(Result.DENY);
            }
            return;
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
            return placeSign(event);
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

    private boolean placeSign(PlayerInteractEvent event) {
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

                BlockFace clickedFace = event.getBlockFace();
                Block signBlock = against.getRelative(clickedFace);
                if (!signBlock.getType().equals(Material.AIR))
                    return false;

                signBlock.setType(Material.WALL_SIGN);
                switch (clickedFace) {
                    case NORTH:
                        signBlock.setData((byte) 4);
                        break;
                    case SOUTH:
                        signBlock.setData((byte) 5);
                        break;
                    case EAST:
                        signBlock.setData((byte) 2);
                        break;
                    case WEST:
                        signBlock.setData((byte) 3);
                        break;
                    default:
                        signBlock.setType(Material.AIR);
                        return false;
                }

                Player player = event.getPlayer();
                DeadboltGroup dbg = DeadboltGroup.getRelated(against);
                Sign sign = (Sign) signBlock.getState();
                if (dbg.getOwner() == null) {
                    Conf.setLine(sign, 0, Conf.default_color_private[0] + "[" + Conf.signtext_private + "]");
                    Conf.setLine(sign, 1, Conf.default_color_private[1] + player.getName());
                } else if (dbg.isOwner(player)) {
                    Conf.setLine(sign, 0, Conf.default_color_moreusers[0] + "[" + Conf.signtext_moreusers + "]");
                } else if (player.hasPermission(Perm.admin_create)) {
                    Conf.setLine(sign, 0, Conf.default_color_moreusers[0] + "[" + Conf.signtext_moreusers + "]");
                    Conf.sendMessage(player, String.format(Conf.msg_admin_sign_placed, dbg.getOwner()), ChatColor.RED);
                } else {
                    Conf.sendMessage(player, String.format(Conf.msg_deny_sign_quickplace, dbg.getOwner()), ChatColor.RED);
                    signBlock.setType(Material.AIR);
                    return false;
                }
                
                sign.update(true);

                ItemStack held = player.getItemInHand();
                held.setAmount(held.getAmount() - 1);
                if (held.getAmount() == 0)
                    player.setItemInHand(null);

                //WorldServer worldServer = ((CraftWorld) player.getWorld()).getHandle();
                //TileEntitySign tileEntitySign = (TileEntitySign) worldServer.getTileEntity(signBlock.getX(), signBlock.getY(), signBlock.getZ());
                //EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                //entityPlayer.a(tileEntitySign);
                return false;
            default:
                return true;
        }
    }

    private boolean onPlayerInteractDoor(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        DeadboltGroup dbg = DeadboltGroup.getRelated(block);
        if (dbg.getOwner() == null) {
            return true;
        } else if (dbg.isOwner(player)) {
            dbg.toggleBlocks(plugin, block.getType());
            return false;
        } else if (player.hasPermission(Perm.admin_bypass)) {
            dbg.toggleBlocks(plugin, block.getType());
            Conf.sendMessage(player, String.format(Conf.msg_admin_bypass,dbg.getOwner()), ChatColor.RED);
            return false;
        } else {
            Conf.sendMessage(player, Conf.msg_deny_access_door, ChatColor.RED);
            return false;
        }
    }

    private boolean onPlayerInteractContainer(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        DeadboltGroup dbg = DeadboltGroup.getRelated(block);
        if (dbg.getOwner() == null) {
            return true;
        } else if (dbg.isOwner(player)) {
            return true;
        } else if (player.hasPermission(Perm.admin_container)) {
            Conf.sendBroadcast(Perm.broadcast_admin_container, String.format(Conf.msg_admin_container, player.getName(), dbg.getOwner()), ChatColor.RED);
            return true;
        } else {
            Conf.sendMessage(player, Conf.msg_deny_access_container, ChatColor.RED);
            return false;
        }
    }

    private boolean onPlayerInteractWallSign(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        DeadboltGroup dbg = DeadboltGroup.getRelated(block);
        if (dbg.getOwner() == null) {
            return true;
        } else if (dbg.isOwner(player)) {
            //((CraftPlayer) player).getHandle().a((TileEntitySign) ((CraftWorld) player.getWorld()).getHandle().getTileEntity(signBlock.getX(), signBlock.getY(), signBlock.getZ()));
            Conf.selectedSign.put(player, block);
            Conf.sendMessage(player, Conf.cmd_sign_selected, ChatColor.GOLD);
            return false;
        } else if (player.hasPermission(Perm.admin_sign_selection)) {
            //((CraftPlayer) player).getHandle().a((TileEntitySign) ((CraftWorld) player.getWorld()).getHandle().getTileEntity(signBlock.getX(), signBlock.getY(), signBlock.getZ()));
            Conf.selectedSign.put(player, block);
            Conf.sendMessage(player, String.format(Conf.msg_admin_sign_selection,dbg.getOwner()), ChatColor.RED);
            return false;
        } else {
            Conf.sendMessage(player, Conf.msg_deny_sign_selection, ChatColor.RED);
            return false;
        }
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        Conf.selectedSign.remove(event.getPlayer());
    }
}
