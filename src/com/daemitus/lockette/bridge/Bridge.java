package com.daemitus.lockette.bridge;

import com.daemitus.lockette.Lockette;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.entity.Player;

public class Bridge {

    private static final Set<Object> objects = new HashSet<Object>();

    /**
     * Register a bridge with Lockette for use in authorizing users to interact with various protected blocks.
     * <br>Requires implementing <pre>com.daemitus.lockette.bridge.LocketteBridge</pre>
     * @param bridge Class to be added
     * @return Success or failure
     */
    public static boolean registerBridge(Object bridge) {
        for (Class<?> myClass : bridge.getClass().getInterfaces()) {
            if (myClass.equals(LocketteBridge.class)) {
                return objects.add(bridge);
            }
        }
        return false;
    }

    /**
     * Unregister a bridge from Lockette
     * @param bridge Class to be removed
     * @return Success or failure
     */
    public static boolean unregisterBridge(Object bridge) {
        return objects.remove(bridge);
    }

    public static void unregisterAll() {
        objects.clear();
        objects.removeAll(objects);
    }

    public static boolean queryBridges(Player player, List<String> names) {
        for (Object obj : objects) {
            try {
                Method method = obj.getClass().getMethod("isAuthorized", Player.class, List.class);
                Object result = method.invoke(obj, player, names);
                boolean authorized = (Boolean) result;
                if (authorized)
                    return true;
            } catch (NoSuchMethodException ex) {
                Lockette.logger.log(Level.SEVERE, "Lockette: " + obj.getClass().getName() + " has no method \"isAuthorized(Player, List)\"", ex);
            } catch (SecurityException ex) {
                Lockette.logger.log(Level.SEVERE, "Lockette: " + obj.getClass().getName() + " Security Exception", ex);
            } catch (IllegalAccessException ex) {
                Lockette.logger.log(Level.SEVERE, "Lockette: " + obj.getClass().getName() + " Illegal Access Exception ", ex);
            } catch (IllegalArgumentException ex) {
                Lockette.logger.log(Level.SEVERE, "Lockette: " + obj.getClass().getName() + " arguments are not of type (Player, List)", ex);
            } catch (InvocationTargetException ex) {
                Lockette.logger.log(Level.SEVERE, "Lockette: " + obj.getClass().getName() + " Invocation Target Exception", ex);
            } catch (ClassCastException ex) {
                Lockette.logger.log(Level.SEVERE, "Lockette: " + obj.getClass().getName() + " has a return other than boolean", ex);
            }
        }
        return false;
    }
}
