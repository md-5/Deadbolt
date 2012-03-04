package com.daemitus.deadbolt;

import com.daemitus.deadbolt.commands.DeadboltCommandExecutor;
import com.daemitus.deadbolt.events.*;
import com.daemitus.deadbolt.listener.ListenerManager;
import com.daemitus.deadbolt.tasks.ToggleDoorTask;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Deadbolt extends JavaPlugin {

    public static Deadbolt instance;
    public static final Logger logger = Bukkit.getServer().getLogger();
    public ListenerManager listenerManager;
    public Config config;

    public Deadbolt() {
        instance = this;
    }

    @Override
    public void onEnable() {
        config = new Config();
        config.load();
        new SignListener();
        new BlockListener();
        new PlayerListener();
        EntityListener e = (config.deny_entity_interact) ? new EntityListener() : null;
        PistonListener p = (config.deny_pistons) ? new PistonListener() : null;
        RedstoneListener r = (config.deny_redstone) ? new RedstoneListener() : null;
        new Deadbolted(this);
        listenerManager = new ListenerManager();
        listenerManager.registerListeners();
        listenerManager.checkListeners();
        new ServerListener();

        getCommand("deadbolt").setExecutor(new DeadboltCommandExecutor(this));
    }

    @Override
    public void onDisable() {
        ToggleDoorTask.cleanup();
    }

    /**
     * Check if <player> or [Everyone] is on any of the [Private] or [More
     * Users] signs associated with <block>
     *
     * @param player Player to be checked
     * @param block Block to be checked
     * @return If <name> is authorized to use <block>
     */
    public static boolean isAuthorized(Player player, Block block) {
        return Deadbolted.get(block).isUser(player);
    }

    /**
     * Check if <block> is protected by <player> or not
     *
     * @param player Player to be checked
     * @param block Block to be checked
     * @return If <player> owns <block>
     */
    public static boolean isOwner(Player player, Block block) {
        return Deadbolted.get(block).isOwner(player);
    }

    /**
     * Retrieves all names authorized to interact with <block>
     *
     * @param block Block to be checked
     * @return A List<String> containing everything on any [Private] or [More
     * Users] signs associated with <block>
     */
    public static Set<String> getAllNames(Block block) {
        return Deadbolted.get(block).getUsers();
    }

    /**
     * Retrieves owner of <block>
     *
     * @param block Block to be checked
     * @return The text on the line below [Private] on the sign associated with
     * <block>. null if unprotected
     */
    public static String getOwnerName(Block block) {
        return Deadbolted.get(block).getOwner();
    }

    /**
     * Check if <block> is protected or not
     *
     * @param block The block to be checked
     * @return If <block> is owned
     */
    public static boolean isProtected(Block block) {
        return Deadbolted.get(block).isProtected();
    }
}
