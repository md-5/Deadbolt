package com.daemitus.deadbolt.bridge;

import com.daemitus.deadbolt.Deadbolt;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Bridge {

    private static final Set<Object> objects = new HashSet<Object>();

    /**
     * Register a bridge with Deadbolt for use in authorizing users to interact with various protected blocks.
     * <br>Requires implementing <pre>com.daemitus.deadbolt.bridge.DeadboltBridge</pre>
     * @param bridge Class to be added
     * @return Success or failure
     */
    public static boolean registerBridge(Object bridge) {
        for (Class<?> myClass : bridge.getClass().getInterfaces()) {
            if (myClass.equals(DeadboltBridge.class)) {
                return objects.add(bridge);
            }
        }
        return false;
    }

    /**
     * Unregister a bridge from Deadbolt
     * @param bridge Class to be removed
     * @return Success or failure
     */
    public static boolean unregisterBridge(Object bridge) {
        return objects.remove(bridge);
    }

    /**
     * Calls the isAuthorized method for each registered Class
     * @param player 
     * @param names
     * @return
     */
    public static boolean isAuthorized(Player player, List<String> names) {
        for (Object obj : objects) {
            try {
                Method method = obj.getClass().getMethod("isAuthorized", Player.class, List.class);
                Object result = method.invoke(obj, player, names);
                boolean authorized = (Boolean) result;
                if (authorized)
                    return true;
            } catch (NoSuchMethodException ex) {
                Deadbolt.logger.log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " has no method \"isAuthorized(Player, List)\"", ex);
            } catch (SecurityException ex) {
                Deadbolt.logger.log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " Security Exception", ex);
            } catch (IllegalAccessException ex) {
                Deadbolt.logger.log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " Illegal Access Exception ", ex);
            } catch (IllegalArgumentException ex) {
                Deadbolt.logger.log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " arguments are not of type (Player, List)", ex);
            } catch (InvocationTargetException ex) {
                Deadbolt.logger.log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " Invocation Target Exception", ex);
            } catch (ClassCastException ex) {
                Deadbolt.logger.log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " has a return other than boolean", ex);
            }
        }
        return false;
    }

    /**
     * Calls the canProtect method for each registered Class
     * @param player
     * @param block
     * @return
     */
    public static boolean canProtect(Player player, Block block) {
        for (Object obj : objects) {
            try {
                Method method = obj.getClass().getMethod("canProtect", Player.class, Block.class);
                Object result = method.invoke(obj, player, block);
                boolean authorized = (Boolean) result;
                if (!authorized)
                    return false;
            } catch (NoSuchMethodException ex) {
                Deadbolt.logger.log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " has no method \"canProtect(Player, Block)\"", ex);
            } catch (SecurityException ex) {
                Deadbolt.logger.log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " Security Exception", ex);
            } catch (IllegalAccessException ex) {
                Deadbolt.logger.log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " Illegal Access Exception ", ex);
            } catch (IllegalArgumentException ex) {
                Deadbolt.logger.log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " arguments are not of type (Player, Block)", ex);
            } catch (InvocationTargetException ex) {
                Deadbolt.logger.log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " Invocation Target Exception", ex);
            } catch (ClassCastException ex) {
                Deadbolt.logger.log(Level.SEVERE, "Deadbolt: " + obj.getClass().getName() + " has a return other than boolean", ex);
            }
        }
        return true;
    }
}
