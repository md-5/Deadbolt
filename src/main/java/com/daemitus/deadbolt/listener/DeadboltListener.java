package com.daemitus.deadbolt.listener;

import com.daemitus.deadbolt.DeadboltPlugin;
import com.daemitus.deadbolt.Deadbolted;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class DeadboltListener implements ListenerInterface {

    @Override
    public void load(final DeadboltPlugin plugin) {
    }

    @Override
    public List<String> getDependencies() {
        return new ArrayList<String>();
    }

    @Override
    public boolean canEntityInteract(Deadbolted db, EntityInteractEvent event) {
        return false;
    }

    @Override
    public boolean canEntityExplode(Deadbolted db, EntityExplodeEvent event) {
        return false;
    }

    @Override
    public boolean canEndermanPickup(Deadbolted db, EntityChangeBlockEvent event) {
        return false;
    }

    @Override
    public boolean canRedstoneChange(Deadbolted db, BlockRedstoneEvent event) {
        return false;
    }

    @Override
    public boolean canPistonExtend(Deadbolted db, BlockPistonExtendEvent event) {
        return false;
    }

    @Override
    public boolean canPistonRetract(Deadbolted db, BlockPistonRetractEvent event) {
        return false;
    }

    @Override
    public boolean canBlockBreak(Deadbolted db, BlockBreakEvent event) {
        return false;
    }

    @Override
    public boolean canPlayerInteract(Deadbolted db, PlayerInteractEvent event) {
        return false;
    }

    @Override
    public boolean canSignChange(Deadbolted db, SignChangeEvent event) {
        return true;
    }

    @Override
    public boolean canSignChangeQuick(Deadbolted db, PlayerInteractEvent event) {
        return true;
    }

    public boolean canBlockBurn(Deadbolted db, BlockBurnEvent event) {
        return true;
    }
}
