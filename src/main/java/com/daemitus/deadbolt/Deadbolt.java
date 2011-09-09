package com.daemitus.deadbolt;

import com.daemitus.deadbolt.commands.DeadboltCommandExecutor;
import com.daemitus.deadbolt.bridge.Bridge;
import com.daemitus.deadbolt.events.BlockListener;
import com.daemitus.deadbolt.events.EntityListener;
import com.daemitus.deadbolt.events.PlayerListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Deadbolt extends JavaPlugin {

    public static final Logger logger = Bukkit.getServer().getLogger();
    public Config cm;

    public void onEnable() {

        PluginManager pm = this.getServer().getPluginManager();

        BlockListener blockListener = new BlockListener(this);
        blockListener.registerEvents(pm);

        EntityListener entityListener = new EntityListener(this);
        entityListener.registerEvents(pm);

        PlayerListener playerListener = new PlayerListener(this);
        playerListener.registerEvents(pm);

        cm = new Config(this);
        cm.load();

        this.getCommand("deadbolt").setExecutor(new DeadboltCommandExecutor(this));

        if (!Util.doorSchedule.start(this)) {
            logger.log(Level.WARNING, String.format("Deadbolt: %1$s", Config.console_error_scheduler_start));
        }

        logger.log(Level.INFO, String.format("Deadbolt v%1$s enabled", this.getDescription().getVersion()));
    }

    public void onDisable() {
        stopDoorSchedule();
        logger.log(Level.INFO, String.format("Deadbolt v%1$s disabled", this.getDescription().getVersion()));
    }

    private void stopDoorSchedule() {
        Util.selectedSign.clear();
        if (!Util.doorSchedule.stop()) {
            logger.log(Level.WARNING, String.format("Deadbolt: %1$s", Config.console_error_scheduler_stop));
        }
    }
    //------------------------------------------------------------------------//

    /**
     * Check if <player> or [Everyone] is on any of the [Private] or [More Users] signs associated with <block>
     * @param player Player to be checked
     * @param block Block to be checked
     * @return If <name> is authorized to use <block>
     */
    public static boolean isAuthorized(Player player, Block block) {
        return Util.isAuthorized(player, block);
    }

    /**
     * Check if <block> is protected by <name> or not
     * @param name Name to be checked
     * @param block Block to be checked
     * @return If <name> owns <block>
     */
    public static boolean isOwner(String name, Block block) {
        return Util.isOwner(name, block);
    }

    /**
     * Retrieves all names authorized to interact with <block>
     * @param block Block to be checked
     * @return A List<String> containing everything on any [Private] or [More Users] signs associated with <block>
     */
    public static List<String> getAllNames(Block block) {
        return Util.getAllNames(block);
    }

    /**
     * Retrieve the block a given wallsign is attached to
     * @param block The wallsign to be checked
     * @return The block that the wallsign in attached to
     */
    public static Block getBlockSignAttachedTo(Block block) {
        return Util.getBlockSignAttachedTo(block);
    }

    /**
     * Retrieves owner of <block>
     * @param block Block to be checked
     * @return The text on the line below [Private] on the sign associated with <block>. "" if unprotected
     */
    public static String getOwnerName(Block block) {
        return Util.getOwnerName(block);
    }

    /**
     * Retrieves the sign block associated with <block>
     * @param block Block to be checked
     * @return The sign block associated with <block>. Null if unprotected
     */
    public static Block getOwnerSign(Block block) {
        return Util.getOwnerSign(block);
    }

    /**
     * Check if <block> is protected or not
     * @param block The block to be checked
     * @return If <block> is owned
     */
    public static boolean isProtected(Block block) {
        return Util.isProtected(block);
    }

    /**
     * Interacts with a (set) of doors, toggling their state if authorized.
     * @param player The player who clicked
     * @param block The block clicked
     * @param override Disregard signs and toggle regardless
     * @return Success or failure
     */
    public static boolean interactDoor(Player player, Block block, boolean override) {
        return Util.interactDoor(player, block, override);
    }

    /**
     * Interacts with a container, opening if authorized
     * @param player The player who clicked
     * @param block The block clicked
     * @param override Disregard signs and toggle regardless
     * @return Success or failure
     */
    public static boolean interactContainer(Player player, Block block, boolean override) {
        return Util.interactContainer(player, block, override);
    }

    /**
     * Interacts with a sign, selecting it for /deadbolt <line> <text> usage if authorized
     * @param player The player who clicked
     * @param block The block clicked
     * @return Success or failure
     */
    public static boolean interactSign(Player player, Block block) {
        return Util.interactSign(player, block);
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
