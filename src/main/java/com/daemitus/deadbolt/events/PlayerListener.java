package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Config;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Util;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Result;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;

public class PlayerListener extends org.bukkit.event.player.PlayerListener {

    private final Deadbolt plugin;

    public PlayerListener(final Deadbolt plugin) {
        this.plugin = plugin;
    }

    public void registerEvents(final PluginManager pm) {
        pm.registerEvent(Type.PLAYER_INTERACT, this, Priority.Normal, plugin);
        pm.registerEvent(Type.PLAYER_QUIT, this, Priority.Normal, plugin);
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            switch (block.getType()) {
                case WOODEN_DOOR:
                case IRON_DOOR_BLOCK:
                case TRAP_DOOR:
                case FENCE_GATE:
                    if (!Util.interactDoor(player, block, false)) {
                        event.setUseInteractedBlock(Result.DENY);
                        event.setUseItemInHand(Result.DENY);
                        Util.sendMessage(player, Config.msg_deny_door_access, ChatColor.RED);
                    }
                    break;
            }
        } else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            switch (block.getType()) {
                case WOODEN_DOOR:
                case IRON_DOOR_BLOCK:
                case TRAP_DOOR:
                case FENCE_GATE:
                    if (!Util.interactDoor(player, block, false)) {
                        event.setUseInteractedBlock(Result.DENY);
                        event.setUseItemInHand(Result.DENY);
                        Util.sendMessage(player, Config.msg_deny_door_access, ChatColor.RED);
                    }
                    break;
                case CHEST:
                case FURNACE:
                case BURNING_FURNACE:
                case DISPENSER:
                    if (!Util.interactContainer(player, block, false)) {
                        event.setUseInteractedBlock(Result.DENY);
                        event.setUseItemInHand(Result.DENY);
                        Util.sendMessage(player, Config.msg_deny_container_access, ChatColor.RED);
                    }
                    break;
                case WALL_SIGN:
                    if (!Util.interactSign(player, block)) {
                        event.setUseInteractedBlock(Result.DENY);
                        event.setUseItemInHand(Result.DENY);
                        Util.sendMessage(player, Config.msg_deny_sign_selection, ChatColor.RED);
                    }
                    break;
            }
        }
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        Util.selectedSign.remove(event.getPlayer());
    }
}
