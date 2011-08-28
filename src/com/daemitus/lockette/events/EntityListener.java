package com.daemitus.lockette.events;

import com.daemitus.lockette.Config;
import com.daemitus.lockette.Lockette;
import com.daemitus.lockette.Util;
import org.bukkit.ChatColor;
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

    public void registerEvents(final PluginManager pm) {
        pm.registerEvent(Type.ENTITY_EXPLODE, this, Priority.Lowest, plugin);
    }

    @Override
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled())
            return;
        if (!Config.explosionProtection)
            return;

        for (Block block : event.blockList()) {
            if (Util.isProtected(block)) {
                event.setCancelled(true);
                if (Config.broadcastTNT && event.getEntity() instanceof TNTPrimed) {
                    for (Entity entity : event.getEntity().getNearbyEntities(Config.broadcastTNTRadius,
                                                                             Config.broadcastTNTRadius,
                                                                             Config.broadcastTNTRadius)) {
                        if (entity instanceof Player) {
                            plugin.sendMessage((Player) entity, Config.msg_tnt_fizzle, ChatColor.YELLOW);
                        }
                    }
                }
                break;
            }
        }
    }
}
