package com.daemitus.lockette.events;

import com.daemitus.lockette.Config;
import com.daemitus.lockette.Lockette;
import com.daemitus.lockette.Util;
import java.util.Set;
import org.bukkit.ChatColor;
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

    public void registerEvents(final PluginManager pm) {
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
                if (!Util.interactDoor(player, block, false)) {
                    event.setUseInteractedBlock(Result.DENY);
                    event.setUseItemInHand(Result.DENY);
                    Util.sendMessage(player, Config.msg_deny_door_access, ChatColor.RED);
                }
            }
        } else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            BlockState state = block.getState();
            MaterialData data = state.getData();
            if (data instanceof Door || data instanceof TrapDoor) {
                if (!Util.interactDoor(player, block, false)) {
                    event.setUseInteractedBlock(Result.DENY);
                    event.setUseItemInHand(Result.DENY);
                    Util.sendMessage(player, Config.msg_deny_door_access, ChatColor.RED);
                }
            } else if (state instanceof ContainerBlock) {
                if (!Util.interactContainer(player, block, false)) {
                    event.setUseInteractedBlock(Result.DENY);
                    event.setUseItemInHand(Result.DENY);
                    Util.sendMessage(player, Config.msg_deny_container_access, ChatColor.RED);
                }
            } else if (block.getType().equals(Material.WALL_SIGN)) {
                if (!Util.interactSign(player, block)) {
                    event.setUseInteractedBlock(Result.DENY);
                    event.setUseItemInHand(Result.DENY);
                    Util.sendMessage(player, Config.msg_deny_sign_selection, ChatColor.RED);
                }
            }
        }
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        Util.selectedSign.remove(event.getPlayer());
    }
}
