package com.daemitus.deadbolt;

import com.daemitus.deadbolt.bridge.Bridge;
import com.daemitus.deadbolt.commands.DeadboltCommandExecutor;
import com.daemitus.deadbolt.events.BlockListener;
import com.daemitus.deadbolt.events.EntityListener;
import com.daemitus.deadbolt.events.PlayerListener;
import com.daemitus.deadbolt.events.SignListener;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Deadbolt extends JavaPlugin {

    @Override
    public void onEnable() {

        new Conf(this);
        new SignListener(this);
        new BlockListener(this);
        new EntityListener(this);
        new PlayerListener(this);

        this.getCommand("deadbolt").setExecutor(new DeadboltCommandExecutor(this));

        Bukkit.getLogger().log(Level.INFO, String.format("Deadbolt v%1$s enabled", this.getDescription().getVersion()));
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().log(Level.INFO, String.format("Deadbolt v%1$s disabled", this.getDescription().getVersion()));
    }

    /**
     * Check if <player> or [Everyone] is on any of the [Private] or [More Users] signs associated with <block>
     * @param player Player to be checked
     * @param block Block to be checked
     * @return If <name> is authorized to use <block>
     */
    public static boolean isAuthorized(Player player, Block block) {
        return DeadboltGroup.getRelated(block).isAuthorized(player);
    }

    /**
     * Check if <block> is protected by <name> or not
     * @param name Name to be checked
     * @param block Block to be checked
     * @return If <name> owns <block>
     */
    public static boolean isOwner(Player player, Block block) {
        return DeadboltGroup.getRelated(block).isOwner(player);
    }

    /**
     * Retrieves all names authorized to interact with <block>
     * @param block Block to be checked
     * @return A List<String> containing everything on any [Private] or [More Users] signs associated with <block>
     */
    public static List<String> getAllNames(Block block) {
        return DeadboltGroup.getRelated(block).getAuthorized();
    }

    /**
     * Retrieves owner of <block>
     * @param block Block to be checked
     * @return The text on the line below [Private] on the sign associated with <block>. null if unprotected
     */
    public static String getOwnerName(Block block) {
        return DeadboltGroup.getRelated(block).getOwner();
    }

    /**
     * Check if <block> is protected or not
     * @param block The block to be checked
     * @return If <block> is owned
     */
    public static boolean isProtected(Block block) {
        return DeadboltGroup.getRelated(block).getOwner() != null;
    }

    /**
     * Register a bridge with Deadbolt for use in authorizing users to interact with various protected blocks.
     * <br>Requires implementing <pre>com.daemitus.deadbolt.bridge.DeadboltBridge</pre>
     * @param bridge Class to be added
     * @return Success or failure
     */
    public static boolean registerBridge(Object bridge) {
        return Bridge.registerBridge(bridge);
    }

    /**
     * Unregister a bridge from Deadbolt
     * @param bridge Class to be removed
     * @return Success or failure
     */
    public static boolean unregisterBridge(Object bridge) {
        return Bridge.unregisterBridge(bridge);
    }
}
