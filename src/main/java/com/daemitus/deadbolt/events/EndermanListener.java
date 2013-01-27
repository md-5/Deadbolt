package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class EndermanListener implements Listener {

    public EndermanListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Deadbolt.getPlugin());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        Block block = event.getBlock();
        Deadbolted db = Deadbolt.get(block);
        if (db.isProtected()) {
            event.setCancelled(true);
        }
    }
}
