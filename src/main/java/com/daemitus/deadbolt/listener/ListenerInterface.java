package com.daemitus.deadbolt.listener;

import com.daemitus.deadbolt.DeadboltPlugin;
import com.daemitus.deadbolt.Deadbolted;
import java.util.List;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public interface ListenerInterface {

    public void load(final DeadboltPlugin plugin);

    public List<String> getDependencies();

    public boolean canEntityInteract(Deadbolted db, EntityInteractEvent event);

    public boolean canEntityExplode(Deadbolted db, EntityExplodeEvent event);

    public boolean canEndermanPickup(Deadbolted db, EntityChangeBlockEvent event);

    public boolean canRedstoneChange(Deadbolted db, BlockRedstoneEvent event);

    public boolean canPistonExtend(Deadbolted db, BlockPistonExtendEvent event);

    public boolean canPistonRetract(Deadbolted db, BlockPistonRetractEvent event);

    public boolean canBlockBreak(Deadbolted db, BlockBreakEvent event);

    public boolean canBlockBurn(Deadbolted db, BlockBurnEvent event);

    public boolean canPlayerInteract(Deadbolted db, PlayerInteractEvent event);

    public boolean canSignChange(Deadbolted db, SignChangeEvent event);

    public boolean canSignChangeQuick(Deadbolted db, PlayerInteractEvent event);
}
