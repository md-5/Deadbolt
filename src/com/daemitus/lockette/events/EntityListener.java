package com.daemitus.lockette.events;

import com.daemitus.lockette.Lockette;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityListener extends org.bukkit.event.entity.EntityListener {

    private final Lockette plugin;

    public EntityListener(final Lockette plugin) {
        this.plugin = plugin;
    }

    public void registerEvents() {
        plugin.pm.registerEvent(Type.ENTITY_EXPLODE, this, Priority.Normal, plugin);
    }

    @Override
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled())
            return;
        if (!plugin.cm.setting_explosion_protection)
            return;

        for (Block block : event.blockList()) {
            if (plugin.logic.isProtected(block)) {
                event.setCancelled(true);
                if (plugin.cm.setting_broadcast_tnt_fizzle && event.getEntity() instanceof TNTPrimed) {
                    for (Entity entity : event.getEntity().getNearbyEntities(plugin.cm.setting_broadcast_tnt_fizzle_radius,
                                                                             plugin.cm.setting_broadcast_tnt_fizzle_radius,
                                                                             plugin.cm.setting_broadcast_tnt_fizzle_radius)) {
                        if (entity instanceof Player) {
                            plugin.logic.sendInfoMessage((Player) entity, plugin.cm.msg_tnt_fizzle_locale);
                        }
                    }
                }
                return;
            }
        }
    }
}
