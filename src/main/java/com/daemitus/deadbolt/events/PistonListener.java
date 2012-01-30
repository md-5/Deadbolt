package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import com.daemitus.deadbolt.listener.ListenerManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

public final class PistonListener implements Listener {

    private final Deadbolt plugin = Deadbolt.instance;

    public PistonListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (event.isCancelled())
            return;
        for (Block block : event.getBlocks()) {
            Deadbolted db = Deadbolted.get(block);
            if (Deadbolted.get(block).isProtected() && !ListenerManager.canPistonExtend(db, event))
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (event.isCancelled())
            return;
        Block piston = event.getBlock();
        Block extension = piston.getRelative(event.getDirection());
        Block block = extension.getRelative(event.getDirection());
        Deadbolted db = Deadbolted.get(block);
        if (db.isProtected() && !ListenerManager.canPistonRetract(db, event)) {
            event.setCancelled(true);
            piston.setData((byte) (piston.getData() ^ 0x8));
            extension.setType(Material.AIR);
        }
    }
}
