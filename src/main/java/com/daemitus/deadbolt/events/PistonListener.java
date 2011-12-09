package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Config;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import com.daemitus.deadbolt.listener.ListenerManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.plugin.PluginManager;

public class PistonListener extends org.bukkit.event.block.BlockListener {

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
        Block piston = event.getBlock();
        BlockFace facing = event.getDirection();
        for (int i = 1; i <= event.getLength() + 1; i++) {
            Block cBlock = piston.getRelative(facing.getModX() * i, facing.getModY() * i, facing.getModZ() * i);
            Deadbolted db = Deadbolted.get(cBlock);
            if (db.isProtected()
                    && ListenerManager.canPistonExtend(db, event))
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
        if (!piston.getType().equals(Material.PISTON_STICKY_BASE))
            return;
        BlockFace facing = event.getDirection();
        Block extension = piston.getRelative(facing);
        Block cBlock = extension.getRelative(facing);
        Deadbolted db = Deadbolted.get(cBlock);
        if (db.isProtected()
                && ListenerManager.canPistonRetract(db, event)) {
            event.setCancelled(true);
            extension.setType(Material.AIR);
            cBlock.setData((byte) (cBlock.getData() ^ 0x8));
        }
    }
}
