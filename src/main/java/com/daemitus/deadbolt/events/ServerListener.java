package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Deadbolt;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.PluginManager;

public final class ServerListener extends org.bukkit.event.server.ServerListener {

    private final Deadbolt plugin;

    public ServerListener(final Deadbolt plugin, final PluginManager pm) {
        this.plugin = plugin;
        pm.registerEvent(Type.PLUGIN_ENABLE, this, Priority.Normal, plugin);
        pm.registerEvent(Type.PLUGIN_DISABLE, this, Priority.Normal, plugin);
    }

    @Override
    public void onPluginEnable(PluginEnableEvent event) {
        plugin.listenerManager.checkListeners();
    }

    @Override
    public void onPluginDisable(PluginDisableEvent event) {
        plugin.listenerManager.checkListeners();
    }
}
