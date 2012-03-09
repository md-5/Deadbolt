package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import com.daemitus.deadbolt.listener.ListenerManager;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Enderman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class EndermanListener implements Listener {

    public EndermanListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Deadbolt.getPlugin());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof Enderman) {
            Block block = event.getBlock();
            Deadbolted db = Deadbolt.get(block);
            if (db.isProtected() && !ListenerManager.canEndermanPickup(db, event)) {
                event.setCancelled(true);
            }
        }
    }
}
