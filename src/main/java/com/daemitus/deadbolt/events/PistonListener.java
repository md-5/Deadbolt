package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Config;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import com.daemitus.deadbolt.listener.ListenerManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.plugin.PluginManager;

public final class PistonListener extends org.bukkit.event.block.BlockListener {

    private final Deadbolt plugin;

    public PistonListener(final Deadbolt plugin, final PluginManager pm) {
        this.plugin = plugin;
        pm.registerEvent(Type.BLOCK_PISTON_EXTEND, this, Priority.Low, plugin);
        pm.registerEvent(Type.BLOCK_PISTON_RETRACT, this, Priority.Low, plugin);
    }

    @Override
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (event.isCancelled())
            return;
        if (!Config.deny_pistons)
            return;
        for (Block block : event.getBlocks()) {
            Deadbolted db = Deadbolted.get(block);
            if (Deadbolted.get(block).isProtected() && !ListenerManager.canPistonExtend(db, event))
                event.setCancelled(true);
        }
    }

    @Override
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (event.isCancelled())
            return;
        if (!Config.deny_pistons)
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
