package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Config;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import com.daemitus.deadbolt.listener.ListenerManager;
import org.bukkit.block.Block;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.plugin.PluginManager;

public final class RedstoneListener extends org.bukkit.event.block.BlockListener {

    private final Deadbolt plugin;

    public RedstoneListener(final Deadbolt plugin, final PluginManager pm) {
        this.plugin = plugin;
        pm.registerEvent(Type.REDSTONE_CHANGE, this, Priority.Low, plugin);
    }

    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        if (!Config.deny_redstone)
            return;
        if (!Config.redstone_protected_blockids.contains(block.getTypeId()))
            return;
        Deadbolted db = Deadbolted.get(block);
        if (db.isProtected() && !ListenerManager.canRedstoneChange(db, event) && !db.isEveryone())
            event.setNewCurrent(event.getOldCurrent());
    }
}
