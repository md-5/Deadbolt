package com.daemitus.deadbolt;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

public class Perm {

    private static final PluginManager pm = Bukkit.getServer().getPluginManager();
    public static final Permission user_create_chest = pm.getPermission("deadbolt.user.create.chest");
    public static final Permission user_create_dispenser = pm.getPermission("deadbolt.user.create.dispenser");
    public static final Permission user_create_door = pm.getPermission("deadbolt.user.create.door");
    public static final Permission user_create_furnace = pm.getPermission("deadbolt.user.create.furnace");
    public static final Permission user_create_trapdoor = pm.getPermission("deadbolt.user.create.trapdoor");
    public static final Permission admin_create = pm.getPermission("deadbolt.admin.create");
    public static final Permission admin_break = pm.getPermission("deadbolt.admin.break");
    public static final Permission admin_bypass = pm.getPermission("deadbolt.admin.bypass");
    public static final Permission admin_signs = pm.getPermission("deadbolt.admin.signs");
    public static final Permission admin_snoop = pm.getPermission("deadbolt.admin.snoop");
    public static final Permission command_reload = pm.getPermission("deadbolt.command.reload");
    public static final Permission admin_broadcast_break = pm.getPermission("deadbolt.broadcast.break");
    public static final Permission admin_broadcast_snoop = pm.getPermission("deadbolt.broadcast.snoop");
}
