package com.daemitus.deadbolt.bridge.essentialsgroupmanager;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.bridge.DeadboltBridge;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.data.Group;
import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class EssentialsGroupManagerBridge extends JavaPlugin implements DeadboltBridge {

    private static final String patternBracketTooLong = "\\[.{14,}\\]";
    private GroupManager gm;

    @Override
    public void onDisable() {
        if (Deadbolt.unregisterBridge(this)) {
            Bukkit.getLogger().log(Level.INFO, "Deadbolt-EssentialsGroupManager: disabled");
        } else {
            Bukkit.getLogger().log(Level.WARNING, "Deadbolt-EssentialsGroupManager: Could not unregister with Deadbolt");
        }
    }

    @Override
    public void onEnable() {
        gm = (GroupManager) this.getServer().getPluginManager().getPlugin("GroupManager");
        if (gm == null) {
            Bukkit.getLogger().log(Level.WARNING, "Deadbolt-EssentialsGroupManager: GroupManager not found");
        } else {
            if (Deadbolt.registerBridge(this)) {
                Bukkit.getLogger().log(Level.INFO, "Deadbolt-EssentialsGroupManager: enabled");
            } else {
                Bukkit.getLogger().log(Level.WARNING, "Deadbolt-EssentialsGroupManager: Could not register with Deadbolt");
            }
        }
    }

    @Override
    public boolean isAuthorized(Player player, List<String> names) {
        OverloadedWorldHolder owh = gm.getWorldsHolder().getWorldData(player);
        Group group = owh.getUser(player.getName()).getGroup();
        Set<String> groupNames = new HashSet<String>();
        groupNames.add(group.getName());
        getInherited(group, groupNames, owh);
        
        for (String gName : groupNames) {
            if (names.contains(truncate("[" + gName + "]").toLowerCase()))
                return true;
        }
        return false;
    }

    private void getInherited(Group group, Set<String> groupNames, OverloadedWorldHolder owh) {
        for (String gs : group.getInherits()) {
            if (groupNames.add(gs)) {
                getInherited(owh.getGroup(gs), groupNames, owh);
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

    @Override
    public boolean isOwner(Player player, Block block) {
        return false;
    }
}
