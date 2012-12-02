package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.DeadboltPlugin;
import com.daemitus.deadbolt.Deadbolted;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

public class RedstoneListener implements Listener {

    private final DeadboltPlugin plugin = Deadbolt.getPlugin();

    public RedstoneListener() {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        if (Deadbolt.getConfig().redstone_protected_blockids.contains(block.getTypeId())) {
            Deadbolted db = Deadbolt.get(block);
            if (db.isProtected() && !db.isEveryone()) {
                event.setNewCurrent(event.getOldCurrent());
            }
        }
    }
}
