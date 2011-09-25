package com.daemitus.deadbolt.bridge;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Bridge {

    private static final Set<Object> objects = new HashSet<Object>();

    public static boolean registerBridge(Object bridge) {
        for (Class<?> myClass : bridge.getClass().getInterfaces()) {
            if (myClass.equals(DeadboltBridge.class)) {
                return objects.add(bridge);
            }
        }
        return false;
    }

    public static boolean unregisterBridge(Object bridge) {
        return objects.remove(bridge);
    }

    public static boolean isOwner(Player player, Block block) {
        for (Object obj : objects) {
            try {
                Method method = obj.getClass().getMethod("isOwner", Player.class, Block.class);
                Object result = method.invoke(obj, player, block);
                boolean owner = (Boolean) result;
                if (owner)
                    return true;
            } catch (NoSuchMethodException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " has no method \"isOwner(Player, Block)\"", ex);
            } catch (SecurityException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " Security Exception", ex);
            } catch (IllegalAccessException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " Illegal Access Exception ", ex);
            } catch (IllegalArgumentException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " arguments are not of type (Player, Block)", ex);
            } catch (InvocationTargetException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " Invocation Target Exception", ex);
            } catch (ClassCastException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " has a return other than boolean", ex);
            }
        }
        return false;
    }

    public static boolean isAuthorized(Player player, List<String> names) {
        for (Object obj : objects) {
            try {
                Method method = obj.getClass().getMethod("isAuthorized", Player.class, List.class);
                Object result = method.invoke(obj, player, names);
                boolean authorized = (Boolean) result;
                if (authorized)
                    return true;
            } catch (NoSuchMethodException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " has no method \"isAuthorized(Player, List)\"", ex);
            } catch (SecurityException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " Security Exception", ex);
            } catch (IllegalAccessException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " Illegal Access Exception ", ex);
            } catch (IllegalArgumentException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " arguments are not of type (Player, List)", ex);
            } catch (InvocationTargetException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " Invocation Target Exception", ex);
            } catch (ClassCastException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " has a return other than boolean", ex);
            }
        }
        return false;
    }

    public static boolean canProtect(Player player, Block block) {
        for (Object obj : objects) {
            try {
                Method method = obj.getClass().getMethod("canProtect", Player.class, Block.class);
                Object result = method.invoke(obj, player, block);
                boolean authorized = (Boolean) result;
                if (!authorized)
                    return false;
            } catch (NoSuchMethodException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " has no method \"canProtect(Player, Block)\"", ex);
            } catch (SecurityException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " Security Exception", ex);
            } catch (IllegalAccessException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " Illegal Access Exception ", ex);
            } catch (IllegalArgumentException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " arguments are not of type (Player, Block)", ex);
            } catch (InvocationTargetException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " Invocation Target Exception", ex);
            } catch (ClassCastException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " has a return other than boolean", ex);
            }
        }
        return true;
    }
}
