package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.*;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;

public class HopperMinecartListener implements Listener {

    private final DeadboltPlugin plugin = Deadbolt.getPlugin();

    public HopperMinecartListener() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
        Inventory initiator = event.getInitiator();

        if (!(initiator.getHolder() instanceof HopperMinecart)) {
            return;
        }

        Inventory other = event.getSource();
        if (other == initiator) {
            other = event.getDestination();
        }

        if (Deadbolt.isProtected(other)) {
            event.setCancelled(true);
        }
    }
}
