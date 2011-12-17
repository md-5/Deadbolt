package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Perm;
import com.daemitus.deadbolt.Config;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import com.daemitus.deadbolt.listener.ListenerManager;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.PluginManager;

public final class BlockListener extends org.bukkit.event.block.BlockListener {

    private final Deadbolt plugin;

    public BlockListener(final Deadbolt plugin, final PluginManager pm) {
        this.plugin = plugin;
        pm.registerEvent(Type.BLOCK_BREAK, this, Priority.Low, plugin);
        pm.registerEvent(Type.BLOCK_PLACE, this, Priority.Normal, plugin);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Deadbolted db = Deadbolted.get(block);
        if (!db.isProtected())
            return;
        if (db.isOwner(player))
            return;
        if (ListenerManager.canBlockBreak(db, event))
            return;
        if (Config.hasPermission(player, Perm.admin_break)) {
            Config.sendBroadcast(Perm.admin_broadcast_break, ChatColor.RED, Config.msg_admin_break, player.getName(), db.getOwner());
            Deadbolt.logger.log(Level.INFO, String.format("[Deadbolt] " + Config.msg_admin_break, player.getName(), db.getOwner()));
            return;
        }

        event.setCancelled(true);
        Config.sendMessage(player, ChatColor.RED, Config.msg_deny_block_break);
        if (block.getType().equals(Material.WALL_SIGN)) {
            //TODO blanked signs after breaking without permission
            //FAILED
            //plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ProtectionRegenTask(block), 1);
            //FAILED 
            //block.setTypeIdAndData(block.getTypeId(), block.getData(), false);
            //FAILED 
            //((Sign)block.getState()).update(true);
            //FAILED 
            //Sign sign = (Sign)block.getState();
            //for(int i = 0; i < 4; i++)
            //    sign.setLine(i, sign.getLine(i));
            //sign.update();
            //FAILED
            //block.setTypeIdAndData(block.getTypeId(), (byte) (block.getData() + 1), false);
            //block.setTypeIdAndData(block.getTypeId(), block.getData() , false);
        }
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        Block against = event.getBlockAgainst();

        if (against.getType().equals(Material.WALL_SIGN) && Config.isValidWallSign((Sign) against.getState())) {
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
                if (db.isProtected()) {
                    if (db.isOwner(player)) {
                        if (Config.reminder.add(player))
                            Config.sendMessage(player, ChatColor.GOLD, Config.msg_reminder_lock_your_chests);
                    } else {
                        event.setCancelled(true);
                        Config.sendMessage(player, ChatColor.RED, Config.msg_deny_container_expansion);
                    }
                }
                return;
            case IRON_DOOR_BLOCK:
            case WOODEN_DOOR:
                if (db.isProtected() && !db.isOwner(player)) {
                    Config.sendMessage(player, ChatColor.RED, Config.msg_deny_door_expansion);
                    Block upBlock = block.getRelative(BlockFace.UP);
                    block.setType(Material.STONE);
                    block.setType(Material.AIR);
                    upBlock.setType(Material.STONE);
                    upBlock.setType(Material.AIR);
                    event.setCancelled(true);
                }
                return;
            case TRAP_DOOR:
                if (db.isProtected() && !db.isOwner(player)) {
                    Config.sendMessage(player, ChatColor.RED, Config.msg_deny_trapdoor_expansion);
                    event.setCancelled(true);
                }
                return;
            case FENCE_GATE:
                if (db.isProtected() && !db.isOwner(player)) {
                    Config.sendMessage(player, ChatColor.RED, Config.msg_deny_fencegate_expansion);
                    event.setCancelled(true);
                }
                return;
        }
    }
}