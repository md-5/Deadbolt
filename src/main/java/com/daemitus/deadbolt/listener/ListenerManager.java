package com.daemitus.deadbolt.listener;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.DeadboltPlugin;
import com.daemitus.deadbolt.Deadbolted;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

public class ListenerManager {

    private final DeadboltPlugin plugin = Deadbolt.getPlugin();
    private static List<ListenerInterface> loaded = new ArrayList<ListenerInterface>();
    private static List<ListenerInterface> unloaded = new ArrayList<ListenerInterface>();

    public void registerListeners() {
        loaded.clear();
        unloaded.clear();
        File dir = new File(plugin.getDataFolder() + "/listeners");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            ClassLoader loader = new URLClassLoader(new URL[]{dir.toURI().toURL()}, ListenerInterface.class.getClassLoader());
            for (File file : dir.listFiles()) {
                String name = file.getName();
                if (!name.endsWith(".class")) {
                    continue;
                }
                Class<?> clazz = loader.loadClass(name.substring(0, name.lastIndexOf(".")));
                Object object = clazz.newInstance();
                if (object instanceof ListenerInterface) {
                    ListenerInterface listener = (ListenerInterface) object;
                    unloaded.add(listener);
                    Deadbolt.getLogger().log(Level.INFO, "[Deadbolt] Registered " + listener.getClass().getSimpleName());
                } else {
                    Deadbolt.getLogger().log(Level.WARNING, String.format("[Deadbolt] " + clazz.getSimpleName() + " does not extend " + DeadboltListener.class.getSimpleName() + " properly"));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void checkListeners() {
        for (Plugin pl : plugin.getServer().getPluginManager().getPlugins()) {
            if (pl.isEnabled()) {
                checkListener(pl);
            }
        }
    }

    public void checkListener(Plugin pl) {
        String name = pl.getDescription().getName();
        for (ListenerInterface listener : unloaded) {
            if (listener.getDependencies().contains(name)) {
                boolean enableListener = true;
                for (String depends : listener.getDependencies()) {
                    enableListener &= Bukkit.getServer().getPluginManager().getPlugin(depends).isEnabled();
                }
                if (enableListener) {
                    if (!loaded.contains(listener)) {
                        loaded.add(listener);
                        listener.load(plugin);
                        Deadbolt.getLogger().info(listener.getClass().getSimpleName() + " is now enabled");
                    }
                } else {
                    if (loaded.contains(listener)) {
                        loaded.remove(listener);
                        Deadbolt.getLogger().info(listener.getClass().getSimpleName() + " disabled due to one or more dependencies");
                    }
                }
            }
        }
    }

    public static boolean canEntityInteract(Deadbolted db, EntityInteractEvent event) {
        boolean allow = false;
        for (ListenerInterface listener : loaded) {
            allow |= listener.canEntityInteract(db, event);
        }
        return allow;
    }

    public static boolean canEntityExplode(Deadbolted db, EntityExplodeEvent event) {
        boolean allow = false;
        for (ListenerInterface listener : loaded) {
            allow |= listener.canEntityExplode(db, event);
        }
        return allow;
    }

    public static boolean canEndermanPickup(Deadbolted db, EntityChangeBlockEvent event) {
        boolean allow = false;
        for (ListenerInterface listener : loaded) {
            allow |= listener.canEndermanPickup(db, event);
        }
        return allow;
    }

    public static boolean canRedstoneChange(Deadbolted db, BlockRedstoneEvent event) {
        boolean allow = false;
        for (ListenerInterface listener : loaded) {
            allow |= listener.canRedstoneChange(db, event);
        }
        return allow;
    }

    public static boolean canPistonExtend(Deadbolted db, BlockPistonExtendEvent event) {
        boolean allow = false;
        for (ListenerInterface listener : loaded) {
            allow |= listener.canPistonExtend(db, event);
        }
        return allow;
    }

    public static boolean canPistonRetract(Deadbolted db, BlockPistonRetractEvent event) {
        boolean allow = false;
        for (ListenerInterface listener : loaded) {
            allow |= listener.canPistonRetract(db, event);
        }
        return allow;
    }

    public static boolean canBlockBreak(Deadbolted db, BlockBreakEvent event) {
        boolean allow = false;
        for (ListenerInterface listener : loaded) {
            allow |= listener.canBlockBreak(db, event);
        }
        return allow;
    }

    public static boolean canBlockBurn(Deadbolted db, BlockBurnEvent event) {
        boolean allow = false;
        for (ListenerInterface listener : loaded) {
            allow |= listener.canBlockBurn(db, event);
        }
        return allow;
    }

    public static boolean canPlayerInteract(Deadbolted db, PlayerInteractEvent event) {
        boolean allow = false;
        for (ListenerInterface listener : loaded) {
            allow |= listener.canPlayerInteract(db, event);
        }
        return allow;
    }

    public static boolean canSignChange(Deadbolted db, SignChangeEvent event) {
        boolean allow = true;
        for (ListenerInterface listener : loaded) {
            allow &= listener.canSignChange(db, event);
        }
        return allow;
    }

    public static boolean canSignChangeQuick(Deadbolted db, PlayerInteractEvent event) {
        boolean allow = true;
        for (ListenerInterface listener : loaded) {
            allow &= listener.canSignChangeQuick(db, event);
        }
        return allow;
    }
}
