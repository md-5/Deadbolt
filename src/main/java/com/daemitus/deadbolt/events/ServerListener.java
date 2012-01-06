package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Deadbolt;
import org.bukkit.Bukkit;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

public final class ServerListener extends org.bukkit.event.server.ServerListener {

    private final Deadbolt plugin = Deadbolt.instance;

    public ServerListener() {
        Bukkit.getServer().getPluginManager().registerEvent(Type.PLUGIN_ENABLE, this, Priority.Normal, plugin);
        Bukkit.getServer().getPluginManager().registerEvent(Type.PLUGIN_DISABLE, this, Priority.Normal, plugin);
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        plugin.listenerManager.checkListener(event.getPlugin());
    }

    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        plugin.listenerManager.checkListener(event.getPlugin());
    }
}
