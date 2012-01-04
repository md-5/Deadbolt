package com.daemitus.deadbolt;

import com.daemitus.deadbolt.commands.DeadboltCommandExecutor;
import com.daemitus.deadbolt.events.RedstoneListener;
import com.daemitus.deadbolt.events.BlockListener;
import com.daemitus.deadbolt.events.EntityListener;
import com.daemitus.deadbolt.events.PistonListener;
import com.daemitus.deadbolt.events.PlayerListener;
import com.daemitus.deadbolt.events.ServerListener;
import com.daemitus.deadbolt.events.SignListener;
import com.daemitus.deadbolt.listener.ListenerManager;
import com.daemitus.deadbolt.tasks.ToggleDoorTask;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Deadbolt extends JavaPlugin {

    public static Deadbolt instance;
    public static final Logger logger = Bukkit.getServer().getLogger();
    public ListenerManager listenerManager;
    public Config config;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        PluginManager pm = this.getServer().getPluginManager();
        config = new Config(this);
        config.load();
        new SignListener();
        new BlockListener();
        new PlayerListener();
        EntityListener e = (Config.deny_entity_interact)?new EntityListener(): null;
        PistonListener p = (Config.deny_pistons) ? new PistonListener(this, pm) : null;
        RedstoneListener r = (Config.deny_redstone) ? new RedstoneListener(this, pm) : null;
        new Deadbolted(this);
        listenerManager = new ListenerManager(this, this.getServer().getPluginManager());
        listenerManager.registerListeners();
        listenerManager.checkListeners();
        new ServerListener(this, pm);

        this.getCommand("deadbolt").setExecutor(new DeadboltCommandExecutor(this));

        logger.log(Level.INFO, "[Deadbolt] " + this.getDescription().getVersion() + " enabled");
    }

    @Override
    public void onDisable() {
        ToggleDoorTask.cleanup();
        logger.log(Level.INFO, "[Deadbolt] " + this.getDescription().getVersion() + " disabled");
    }

    /**
     * Check if <player> or [Everyone] is on any of the [Private] or [More Users] signs associated with <block>
     * @param player Player to be checked
     * @param block Block to be checked
     * @return If <name> is authorized to use <block>
     */
    public static boolean isAuthorized(Player player, Block block) {
        return Deadbolted.get(block).isUser(player);
    }

    /**
     * Check if <block> is protected by <player> or not
     * @param player Player to be checked
     * @param block Block to be checked
     * @return If <player> owns <block>
     */
    public static boolean isOwner(Player player, Block block) {
        return Deadbolted.get(block).isOwner(player);
    }

    /**
     * Retrieves all names authorized to interact with <block>
     * @param block Block to be checked
     * @return A List<String> containing everything on any [Private] or [More Users] signs associated with <block>
     */
    public static List<String> getAllNames(Block block) {
        return Deadbolted.get(block).getUsers();
    }

    /**
     * Retrieves owner of <block>
     * @param block Block to be checked
     * @return The text on the line below [Private] on the sign associated with <block>. null if unprotected
     */
    public static String getOwnerName(Block block) {
        return Deadbolted.get(block).getOwner();
    }

    /**
     * Check if <block> is protected or not
     * @param block The block to be checked
     * @return If <block> is owned
     */
    public static boolean isProtected(Block block) {
        return Deadbolted.get(block).isProtected();
    }
}
