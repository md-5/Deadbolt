package com.daemitus.deadbolt;

import com.daemitus.deadbolt.events.*;
import com.daemitus.deadbolt.listener.ListenerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class DeadboltPlugin extends JavaPlugin implements Listener {

    public ListenerManager listenerManager;

    @Override
    public void onEnable() {
        Deadbolt.setPlugin(this);
        Deadbolt.setConfig(new Config());

        new BlockListener();
        new PlayerListener();
        new SignListener();
        if (Deadbolt.getConfig().deny_endermen) {
            new ExplosionListener();
        }
        if (Deadbolt.getConfig().deny_explosions) {
            new EntityListener();
        }
        if (Deadbolt.getConfig().deny_entity_interact) {
            new EntityInteractListener();
        }
        if (Deadbolt.getConfig().deny_pistons) {
            new PistonListener();
        }
        if (Deadbolt.getConfig().deny_redstone) {
            new RedstoneListener();
        }

        listenerManager = new ListenerManager();
        listenerManager.registerListeners();
        listenerManager.checkListeners();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("deadbolt").setExecutor(new DeadboltCommandExecutor(this));
    }

    @Override
    public void onDisable() {
        ToggleDoorTask.cleanup();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Deadbolt.getConfig().selectedSign.remove(event.getPlayer());
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        listenerManager.checkListener(event.getPlugin());
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        listenerManager.checkListener(event.getPlugin());
    }
}
