package com.daemitus.lockette;

import org.bukkit.entity.Player;

public class Perm {

    public static final String user_create_chest = "lockette.user.create.chest";
    public static final String user_create_dispenser = "lockette.user.create.dispenser";
    public static final String user_create_door = "lockette.user.create.door";
    public static final String user_create_furnace = "lockette.user.create.furnace";
    public static final String user_create_trapdoor = "lockette.user.create.trapdoor";
    public static final String admin_create = "lockette.admin.create";
    public static final String admin_break = "lockette.admin.break";
    public static final String admin_bypass = "lockette.admin.bypass";
    public static final String admin_signs = "lockette.admin.signs";
    public static final String admin_snoop = "lockette.admin.snoop";
    public static final String command_reload = "lockette.command.reload";
    public static final String admin_broadcast_break = "lockette.broadcast.break";
    public static final String admin_broadcast_snoop = "lockette.broadcast.snoop";

    /**
     * Check if a player is either an op, or has the permission and if either is enabled
     * @param player Player to be checked
     * @param perm Permission node to be checked
     * @return (useOpList && player.isOp()) || (usePermissions && player.hasPermission(perm))
     */
    public static boolean override(Player player, String perm) {
        return isOp(player) || hasPerm(player, perm);
    }

    /**
     * Check if a player is in ops.txt and if using the list is enabled
     * @param player Player to be checked
     * @return useOpList && player.isOp()
     */
    public static boolean isOp(Player player) {
        return Config.useOpList && player.isOp();
    }

    /**
     * Check if a player has the super perm and if using super permissions is enabled
     * @param player Player to be checked
     * @param perm Permission node to be checked
     * @return usePermissions && player.hasPermission(perm)
     */
    public static boolean hasPerm(Player player, String perm) {
        return Config.usePermissions && player.hasPermission(perm);
    }
}
