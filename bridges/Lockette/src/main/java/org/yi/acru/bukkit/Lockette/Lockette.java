package org.yi.acru.bukkit.Lockette;

import com.daemitus.deadbolt.Deadbolt;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

public class Lockette extends JavaPlugin {

    public void onDisable() {
    }

    public void onEnable() {
    }

    public static boolean isProtected(Block block) {
        return Deadbolt.isProtected(block);
    }

    public static String getProtectedOwner(Block block) {
        String name = Deadbolt.getOwnerName(block);
        return name.isEmpty() ? null : name;
    }
}
