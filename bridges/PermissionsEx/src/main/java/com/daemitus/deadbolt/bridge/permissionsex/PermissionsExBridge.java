package com.daemitus.deadbolt.bridge.permissionsex;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.bridge.DeadboltBridge;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PermissionsExBridge extends JavaPlugin implements DeadboltBridge {

    public static final Logger logger = Logger.getLogger("Minecraft");
    private static final String patternBracketTooLong = "\\[.{14,}\\]";
    private PermissionManager permissions;

    public void onDisable() {
        if (Deadbolt.unregisterBridge(this)) {
            logger.log(Level.INFO, "Deadbolt-PermissionsEx: disabled");
        } else {
            logger.log(Level.WARNING, "Deadbolt-PermissionsEx: Could not unregister with Deadbolt");
        }
    }

    public void onEnable() {
        if (this.getServer().getPluginManager().getPlugin("PermissionsEx") == null) {
            logger.log(Level.WARNING, "Deadbolt-PermissionsEx: PermissionsEx not found");
        } else {
            permissions = PermissionsEx.getPermissionManager();
            if (Deadbolt.registerBridge(this)) {
                logger.log(Level.INFO, "Deadbolt-PermissionsEx: enabled");
            } else {
                logger.log(Level.WARNING, "Deadbolt-PermissionsEx: Could not register with Deadbolt");
            }
        }
    }

    public boolean isAuthorized(Player player, List<String> names) {
        Set<String> allGroupNames = new HashSet<>();
        for (PermissionGroup g : permissions.getUser(player).getGroups()) {
            allGroupNames.add(g.getName());
            getInherited(g, allGroupNames);
        }

        for (String group : allGroupNames) {
            if (names.contains(truncate("[" + group + "]").toLowerCase()))
                return true;
        }
        return false;
    }

    private void getInherited(PermissionGroup group, Set<String> groupNames) {
        for (PermissionGroup g : group.getParentGroups()) {
            if (groupNames.add(g.getName())) {
                getInherited(g, groupNames);
            }
        }
    }

    public boolean canProtect(Player player, Block block) {
        //Not used
        return true;
    }

    private String truncate(String text) {
        if (text.matches(patternBracketTooLong))
            return "[" + text.substring(1, 14) + "]";
        return text;
    }
}
