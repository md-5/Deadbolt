package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import com.daemitus.deadbolt.listener.ListenerManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityListener implements Listener {

    public EntityListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Deadbolt.getPlugin());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (Deadbolt.getConfig().deny_explosions) {
            for (Block block : event.blockList()) {
                Deadbolted db = Deadbolt.get(block);
                if (db.isProtected() && !ListenerManager.canEntityExplode(db, event)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
