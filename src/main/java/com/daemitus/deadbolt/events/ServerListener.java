package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Deadbolt;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

public final class ServerListener implements Listener {

    private final Deadbolt plugin = Deadbolt.instance;

    public ServerListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPluginEnable(PluginEnableEvent event) {
        plugin.listenerManager.checkListener(event.getPlugin());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPluginDisable(PluginDisableEvent event) {
        plugin.listenerManager.checkListener(event.getPlugin());
    }
}
