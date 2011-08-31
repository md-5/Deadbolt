package com.daemitus.lockette.bridge.permissionsbukkit;

import com.daemitus.lockette.Lockette;
import com.daemitus.lockette.bridge.LocketteBridge;
import com.platymuus.bukkit.permissions.Group;
import com.platymuus.bukkit.permissions.PermissionsPlugin;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class LocketteBridge_PermissionsBukkit extends JavaPlugin implements LocketteBridge {

    public static final Logger logger = Logger.getLogger("Minecraft");
    private static final String patternBracketTooLong = "\\[.{14,}\\]";
    private PermissionsPlugin permissions;

    public void onDisable() {
    }

    public void onEnable() {
        permissions = (PermissionsPlugin) this.getServer().getPluginManager().getPlugin("PermissionsBukkit");

        if (permissions == null) {
            logger.log(Level.WARNING, "LocketteBridge_PermissionsBukkit: PermissionsBukkit not found");
        } else {
            if (Lockette.registerBridge(this)) {
                logger.log(Level.INFO, "LocketteBridge_PermissionsBukkit: enabled");
            } else {
                logger.log(Level.WARNING, "LocketteBridge_PermissionsBukkit: Could not register with Lockette");
            }
        }

    }

    public boolean isAuthorized(Player player, List<String> names) {
        Set<String> allGroupNames = new HashSet<String>();
        for (Group g : permissions.getGroups(player.getName())) {
            allGroupNames.add(g.getName());
            getInherited(g, allGroupNames);
        }
        for (String group : allGroupNames) {
            if (names.contains(truncate("[" + group + "]").toLowerCase()))
                return true;
        }
        return false;
    }

    private void getInherited(Group group, Set<String> groupNames) {
        for (Group g : group.getInfo().getGroups()) {
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
