package com.daemitus.lockette.events;

import com.daemitus.lockette.ConfigManager;
import com.daemitus.lockette.Lockette;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.PluginManager;

public class EntityListener extends org.bukkit.event.entity.EntityListener {

    private final Lockette plugin;

    public EntityListener(final Lockette plugin) {
        this.plugin = plugin;
    }

    public void registerEvents(PluginManager pm) {
        pm.registerEvent(Type.ENTITY_EXPLODE, this, Priority.Normal, plugin);
    }

    @Override
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled())
            return;
        if (!ConfigManager.setting_Explosion_Protection)
            return;

        for (Block block : event.blockList()) {
            if (plugin.logic.isProtected(block)) {
                event.setCancelled(true);
                if (ConfigManager.setting_Broadcast_TNT_Fizzle && event.getEntity() instanceof TNTPrimed) {
                    for (Entity entity : event.getEntity().getNearbyEntities(ConfigManager.setting_Broadcast_TNT_Fizzle_Radius,
                                                                             ConfigManager.setting_Broadcast_TNT_Fizzle_Radius,
                                                                             ConfigManager.setting_Broadcast_TNT_Fizzle_Radius)) {
                        if (entity instanceof Player) {
                            plugin.sendMessage((Player) entity, "msg-tnt-fizzle", false);
                        }
                    }
                }
                break;
            }
        }
    }
}
