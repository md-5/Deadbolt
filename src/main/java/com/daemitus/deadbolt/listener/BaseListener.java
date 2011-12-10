package com.daemitus.deadbolt.listener;


import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import com.daemitus.deadbolt.listener.DeadboltListener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EndermanPickupEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BaseListener implements DeadboltListener {

    @Override
    public void load(final Deadbolt plugin) {
    }

    @Override
    public boolean canEntityExplode(Deadbolted db, EntityExplodeEvent event) {
        return false;
    }

    @Override
    public boolean canEndermanPickup(Deadbolted db, EndermanPickupEvent event) {
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
}
