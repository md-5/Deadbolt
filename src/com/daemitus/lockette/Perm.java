package com.daemitus.lockette;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

public class Perm {

    private static final PluginManager pm = Bukkit.getServer().getPluginManager();
    public static final Permission user_create_chest = pm.getPermission("lockette.user.create.chest");
    public static final Permission user_create_dispenser = pm.getPermission("lockette.user.create.dispenser");
    public static final Permission user_create_door = pm.getPermission("lockette.user.create.door");
    public static final Permission user_create_furnace = pm.getPermission("lockette.user.create.furnace");
    public static final Permission user_create_trapdoor = pm.getPermission("lockette.user.create.trapdoor");
    public static final Permission admin_create = pm.getPermission("lockette.admin.create");
    public static final Permission admin_break = pm.getPermission("lockette.admin.break");
    public static final Permission admin_bypass = pm.getPermission("lockette.admin.bypass");
    public static final Permission admin_signs = pm.getPermission("lockette.admin.signs");
    public static final Permission admin_snoop = pm.getPermission("lockette.admin.snoop");
    public static final Permission command_reload = pm.getPermission("lockette.command.reload");
    public static final Permission admin_broadcast_break = pm.getPermission("lockette.broadcast.break");
    public static final Permission admin_broadcast_snoop = pm.getPermission("lockette.broadcast.snoop");
}
