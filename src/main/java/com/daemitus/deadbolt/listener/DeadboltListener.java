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

    public void load(DeadboltPlugin plugin) {
    }

    public List<String> getDependencies() {
        return new ArrayList<String>();
    }

    public boolean canEntityInteract(Deadbolted db, EntityInteractEvent event) {
        return false;
    }

    public boolean canEntityExplode(Deadbolted db, EntityExplodeEvent event) {
        return false;
    }

    public boolean canEndermanPickup(Deadbolted db, EntityChangeBlockEvent event) {
        return false;
    }

    public boolean canRedstoneChange(Deadbolted db, BlockRedstoneEvent event) {
        return false;
    }

    public boolean canPistonExtend(Deadbolted db, BlockPistonExtendEvent event) {
        return false;
    }

    public boolean canPistonRetract(Deadbolted db, BlockPistonRetractEvent event) {
        return false;
    }

    public boolean canBlockBreak(Deadbolted db, BlockBreakEvent event) {
        return false;
    }

    public boolean canBlockBurn(Deadbolted db, BlockBurnEvent event) {
        return false;
    }

    public boolean canPlayerInteract(Deadbolted db, PlayerInteractEvent event) {
        return false;
    }

    public boolean canSignChange(Deadbolted db, SignChangeEvent event) {
        return true;
    }

    public boolean canSignChangeQuick(Deadbolted db, PlayerInteractEvent event) {
        return true;
    }
}
