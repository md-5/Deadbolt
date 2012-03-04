package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import com.daemitus.deadbolt.listener.ListenerManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Enderman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;

public class EntityListener implements Listener {

    private final Deadbolt plugin = Deadbolt.instance;

    public EntityListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityInteract(EntityInteractEvent event) {
        Block block = event.getBlock();
        Deadbolted db = Deadbolted.get(block);
        if (db.isProtected() && !ListenerManager.canEntityInteract(db, event)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (plugin.config.deny_endermen) {
            if (event.getEntity() instanceof Enderman) {
                Block block = event.getBlock();
                Deadbolted db = Deadbolted.get(block);
                if (db.isProtected() && !ListenerManager.canEndermanPickup(db, event)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (plugin.config.deny_explosions) {
            for (Block block : event.blockList()) {
                Deadbolted db = Deadbolted.get(block);
                if (db.isProtected() && !ListenerManager.canEntityExplode(db, event)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
