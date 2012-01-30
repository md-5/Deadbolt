package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import com.daemitus.deadbolt.listener.ListenerManager;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EndermanPickupEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;

public final class EntityListener implements Listener {

    private final Deadbolt plugin = Deadbolt.instance;

    public EntityListener() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityInteract(EntityInteractEvent event) {
        if (event.isCancelled())
            return;
        Block block = event.getBlock();
        Deadbolted db = Deadbolted.get(block);
        if (db.isProtected() && !ListenerManager.canEntityInteract(db, event))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEndermanPickup(EndermanPickupEvent event) {
        if (event.isCancelled())
            return;
        if (!plugin.config.deny_endermen)
            return;
        Block block = event.getBlock();
        Deadbolted db = Deadbolted.get(block);
        if (db.isProtected() && !ListenerManager.canEndermanPickup(db, event))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled())
            return;
        if (!plugin.config.deny_explosions)
            return;
        for (Block block : event.blockList()) {
            Deadbolted db = Deadbolted.get(block);
            if (db.isProtected() && !ListenerManager.canEntityExplode(db, event)) {
                event.setCancelled(true);
                return;
            }
        }
    }
}