package com.daemitus.lockette.bridge.permissionsex;

import com.daemitus.lockette.Lockette;
import com.daemitus.lockette.bridge.LocketteBridge;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class LocketteBridge_PermissionsEx extends JavaPlugin implements LocketteBridge {

    public static final Logger logger = Logger.getLogger("Minecraft");
    private static final String patternBracketTooLong = "\\[.{14,}\\]";
    private PermissionManager permissions;

    public void onDisable() {
    }

    public void onEnable() {
        if (this.getServer().getPluginManager().getPlugin("PermissionsEx") == null) {
            logger.log(Level.WARNING, "LocketteBridge_PermissionsEx: PermissionsEx not found");
        } else {
            permissions = PermissionsEx.getPermissionManager();
            if (Lockette.registerBridge(this)) {
                logger.log(Level.INFO, "LocketteBridge_PermissionsEx: enabled");
            } else {
                logger.log(Level.WARNING, "LocketteBridge_PermissionsEx: Could not register with Lockette");
            }
        }
    }

    public boolean isAuthorized(Player player, List<String> names) {
        Set<String> allGroupNames = new HashSet<String>();
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

    private String truncate(String text) {
        if (text.matches(patternBracketTooLong))
            return "[" + text.substring(1, 14) + "]";
        return text;
    }
}
