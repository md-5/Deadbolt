package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

public class PistonListener implements Listener {

    public PistonListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, Deadbolt.getPlugin());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            Deadbolted db = Deadbolt.get(block);
            if (db.isProtected()) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        Block piston = event.getBlock();
        Block extension = piston.getRelative(event.getDirection());
        Block block = extension.getRelative(event.getDirection());
        Deadbolted db = Deadbolt.get(block);
        if (db.isProtected()) {
            // TODO why cant we just cancel
            event.setCancelled(true);
            piston.setData((byte) (piston.getData() ^ 0x8));
            extension.setType(Material.AIR);
        }
    }
}
