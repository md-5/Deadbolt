package com.daemitus.deadbolt.events;

import java.util.Iterator;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ExplosionListener implements Listener {

    public ExplosionListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Deadbolt.getPlugin());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        Iterator<Block> iter = event.blockList().iterator();
        while (iter.hasNext()) {
            Block block = iter.next();
            Deadbolted db = Deadbolt.get(block);
            if (db.isProtected()) {
                iter.remove();
            }
        }
    }
}
