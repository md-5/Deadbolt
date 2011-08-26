package com.daemitus.lockette.events;

import com.daemitus.lockette.Lockette;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.ContainerBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Result;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;
import org.bukkit.material.TrapDoor;
import org.bukkit.plugin.PluginManager;

public class PlayerListener extends org.bukkit.event.player.PlayerListener {

    private final Lockette plugin;

    public PlayerListener(final Lockette plugin) {
        this.plugin = plugin;
    }

    public void registerEvents(PluginManager pm) {
        pm.registerEvent(Type.PLAYER_INTERACT, this, Priority.Normal, plugin);
        pm.registerEvent(Type.PLAYER_QUIT, this, Priority.Normal, plugin);
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            BlockState state = block.getState();
            MaterialData data = state.getData();
            if (data instanceof Door || data instanceof TrapDoor) {
                if (!plugin.logic.interactDoor(player, block)) {
                    event.setUseInteractedBlock(Result.DENY);
                    event.setUseItemInHand(Result.DENY);
                    plugin.sendMessage(player, "msg-deny-door", true);
                }
            }
        } else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            BlockState state = block.getState();
            MaterialData data = state.getData();
            if (data instanceof Door || data instanceof TrapDoor) {
                if (!plugin.logic.interactDoor(player, block)) {
                    event.setUseInteractedBlock(Result.DENY);
                    event.setUseItemInHand(Result.DENY);
                    plugin.sendMessage(player, "msg-deny-door", true);
                }
            } else if (state instanceof ContainerBlock) {
                if (!plugin.logic.interactContainer(player, block)) {
                    event.setUseInteractedBlock(Result.DENY);
                    event.setUseItemInHand(Result.DENY);
                    plugin.sendMessage(player, "msg-deny-container", true);
                }
            } else if (block.getType().equals(Material.WALL_SIGN)) {
                if (!plugin.logic.interactSign(player, block)) {
                    event.setUseInteractedBlock(Result.DENY);
                    event.setUseItemInHand(Result.DENY);
                    plugin.sendMessage(player, "msg-deny-sign", true);
                }
            }
        }
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.logic.clearSelectedSign(event.getPlayer());
    }
}
