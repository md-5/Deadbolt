package com.daemitus.deadbolt.listener;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EndermanPickupEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ListenerManager {

    private final Deadbolt plugin;
    private static List<DeadboltListener> listeners;

    public ListenerManager(final Deadbolt plugin) {
        this.plugin = plugin;
    }

    public void load(String directory) {
        File dir = new File(directory);
        if (!dir.exists())
            dir.mkdirs();
        listeners = new ArrayList<DeadboltListener>();
        try {
            ClassLoader loader = new URLClassLoader(new URL[]{dir.toURI().toURL()}, DeadboltListener.class.getClassLoader());
            for (File file : dir.listFiles()) {
                String name = file.getName();
                if (!name.endsWith(".class"))
                    continue;
                Class<?> clazz = loader.loadClass(name.substring(0, name.lastIndexOf(".")));
                Object object = clazz.newInstance();
                if (object instanceof DeadboltListener) {
                    DeadboltListener listener = (DeadboltListener) object;
                    listeners.add(listener);
                    listener.load(plugin);
                    Deadbolt.logger.log(Level.INFO, String.format("Deadbolt: Loaded interface %1$s", listener.getClass().getSimpleName()));
                } else {
                    Deadbolt.logger.log(Level.WARNING, String.format("Deadbolt: %1$s does not inherit DeadboltListener properly", clazz.getSimpleName()));
                }
            }
        } catch (InstantiationException ex) {
            Deadbolt.logger.log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Deadbolt.logger.log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Deadbolt.logger.log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Deadbolt.logger.log(Level.SEVERE, null, ex);
        }
    }

    public static boolean canEntityExplode(Deadbolted db, EntityExplodeEvent event) {
        boolean allow = false;
        for (DeadboltListener listener : listeners)
            allow |= listener.canEntityExplode(db, event);
        return allow;
    }

    public static boolean canEndermanPickup(Deadbolted db, EndermanPickupEvent event) {
        boolean allow = false;
        for (DeadboltListener listener : listeners)
            allow |= listener.canEndermanPickup(db, event);
        return allow;
    }

    public static boolean canRedstoneChange(Deadbolted db, BlockRedstoneEvent event) {
        boolean allow = false;
        for (DeadboltListener listener : listeners)
            allow |= listener.canRedstoneChange(db, event);
        return allow;
    }

    public static boolean canPistonExtend(Deadbolted db, BlockPistonExtendEvent event) {
        boolean allow = false;
        for (DeadboltListener listener : listeners)
            allow |= listener.canPistonExtend(db, event);
        return allow;
    }

    public static boolean canPistonRetract(Deadbolted db, BlockPistonRetractEvent event) {
        boolean allow = false;
        for (DeadboltListener listener : listeners)
            allow |= listener.canPistonRetract(db, event);
        return allow;
    }

    public static boolean canBlockBreak(Deadbolted db, BlockBreakEvent event) {
        boolean allow = false;
        for (DeadboltListener listener : listeners)
            allow |= listener.canBlockBreak(db, event);
        return allow;
    }

    public static boolean canPlayerInteract(Deadbolted db, PlayerInteractEvent event) {
        boolean allow = false;
        for (DeadboltListener listener : listeners)
            allow |= listener.canPlayerInteract(db, event);
        return allow;
    }

    public static boolean canSignChange(Deadbolted db, SignChangeEvent event) {
        boolean allow = true;
        for (DeadboltListener listener : listeners)
            allow &= listener.canSignChange(db, event);
        return allow;
    }

    public static boolean canSignChangeQuick(Deadbolted db, PlayerInteractEvent event) {
        boolean allow = true;
        for (DeadboltListener listener : listeners)
            allow &= listener.canSignChangeQuick(db, event);
        return allow;
    }
}