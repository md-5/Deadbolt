package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityInteractEvent;

public class EntityInteractListener implements Listener {

    public EntityInteractListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Deadbolt.getPlugin());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityInteract(EntityInteractEvent event) {
        Block block = event.getBlock();
        Deadbolted db = Deadbolt.get(block);
        if (db.isProtected()) {
            event.setCancelled(true);
        }
    }
}
