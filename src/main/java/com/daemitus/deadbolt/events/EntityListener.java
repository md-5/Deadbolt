package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Conf;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.DeadboltGroup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityListener extends org.bukkit.event.entity.EntityListener {

    private final Deadbolt plugin;

    public EntityListener(final Deadbolt plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvent(Type.ENTITY_EXPLODE, this, Priority.Highest, plugin);
    }

    @Override
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled())
            return;
        if (!Conf.explosionProtection)
            return;
        for (Block block : event.blockList()) {
            if (DeadboltGroup.getRelated(block).getOwner() != null) {
                event.setCancelled(true);
                if (Conf.broadcastTNT && event.getEntity() instanceof TNTPrimed) {
                    for (Entity entity : event.getEntity().getNearbyEntities(Conf.broadcastTNTRadius,
                            Conf.broadcastTNTRadius,
                            Conf.broadcastTNTRadius)) {
                        if (entity instanceof Player) {
                            Conf.sendMessage((Player) entity, Conf.msg_tnt_fizzle, ChatColor.YELLOW);
                        }
                    }
                }
                return;
            }
        }
    }
}
