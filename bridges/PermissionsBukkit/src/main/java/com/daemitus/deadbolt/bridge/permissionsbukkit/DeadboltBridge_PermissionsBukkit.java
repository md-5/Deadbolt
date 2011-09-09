package com.daemitus.deadbolt.bridge.permissionsbukkit;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.bridge.DeadboltBridge;
import com.platymuus.bukkit.permissions.Group;
import com.platymuus.bukkit.permissions.PermissionsPlugin;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class DeadboltBridge_PermissionsBukkit extends JavaPlugin implements DeadboltBridge {

    public static final Logger logger = Logger.getLogger("Minecraft");
    private static final String patternBracketTooLong = "\\[.{14,}\\]";
    private PermissionsPlugin permissions;

    @Override
    public void onDisable() {
        if (Deadbolt.unregisterBridge(this)) {
            logger.log(Level.INFO, "DeadboltBridge_PermissionsBukkit: disabled");
        } else {
            logger.log(Level.WARNING, "DeadboltBridge_PermissionsBukkit: Could not unregister with Deadbolt");
        }
    }

    @Override
    public void onEnable() {
        permissions = (PermissionsPlugin) this.getServer().getPluginManager().getPlugin("PermissionsBukkit");

        if (permissions == null) {
            logger.log(Level.WARNING, "DeadboltBridge_PermissionsBukkit: PermissionsBukkit not found");
        } else {
            if (Deadbolt.registerBridge(this)) {
                logger.log(Level.INFO, "DeadboltBridge_PermissionsBukkit: enabled");
            } else {
                logger.log(Level.WARNING, "DeadboltBridge_PermissionsBukkit: Could not register with Deadbolt");
            }
        }

    }

    @Override
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

    @Override
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
