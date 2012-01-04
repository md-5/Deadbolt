package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import com.daemitus.deadbolt.listener.ListenerManager;
import org.bukkit.block.Block;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EndermanPickupEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;

public final class EntityListener extends org.bukkit.event.entity.EntityListener {

    private final Deadbolt plugin = Deadbolt.instance;

    public EntityListener() {
        plugin.getServer().getPluginManager().registerEvent(Type.ENTITY_INTERACT, this, Priority.High, plugin);
        plugin.getServer().getPluginManager().registerEvent(Type.ENTITY_EXPLODE, this, Priority.High, plugin);
        plugin.getServer().getPluginManager().registerEvent(Type.ENDERMAN_PICKUP, this, Priority.High, plugin);
    }

    @Override
    public void onEntityInteract(EntityInteractEvent event) {
        if (event.isCancelled())
            return;
        Block block = event.getBlock();
        Deadbolted db = Deadbolted.get(block);
        if (db.isProtected() && !ListenerManager.canEntityInteract(db, event))
            event.setCancelled(true);
    }

    @Override
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

    @Override
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

        /*
        Set<Block> protectedBlocks = new HashSet<Block>();
        for (Block block : event.blockList()) {
        if (!protectedBlocks.contains(block)) {
        Deadbolted db = Deadbolted.get(block);
        if (db.isProtected() && !ListenerManager.canEntityExplode(db, event))
        protectedBlocks.addAll(db.getBlocks());
        }
        }
        for (Block block : protectedBlocks)
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ProtectionRegenTask(block), 1);
         */
    }
}