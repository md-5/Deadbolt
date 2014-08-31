package com.daemitus.deadbolt;

import java.util.Map;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;

public class PlayerNameUtil implements Listener {
    
    // -------------------------------------------- //
    // FIELDS
    // -------------------------------------------- //
    
    /**
     * This is the latest created instance of this class.
     */
    public static PlayerNameUtil i = null;

    /**
     * This map is used to improve the speed of name start lookups.
     * Note that the keys in this map is lowercase.
     */
    protected static Map<String, String> lowerCaseStartOfNameToCorrectNames = new TreeMap<String, String>();

    /**
     * Did we populate the map yet?
     */
    protected static boolean isPopulated = false;
    
    // -------------------------------------------- //
    // CONSTRUCTOR AND EVENT LISTENER
    // -------------------------------------------- //
    
    public PlayerNameUtil(Plugin plugin) {
        i = this;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        if ( !isPopulated) {
            populateCaseInsensitiveNameToCaseCorrectName();
            isPopulated = true;
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    // We can't use AsyncPlayerPreLoginEvent because that's not reliable on CraftBukkit
    public synchronized void onLowestPlayerPreLoginEvent(PlayerLoginEvent event)
    {
        checkPlayer(event.getPlayer());
    }
    
    // -------------------------------------------- //
    // PUBLIC METHODS
    // -------------------------------------------- //
    
    /**
     * In Minecraft a playername can be 16 characters long. One sign line is however only 15 characters long.
     * If we find a 15 character long playername on a sign it could thus refer to more than one player.
     * This method finds all possible matching player names.
     */
    public static String interpretSignName(String playerNameFromSign) {
        return playerNameFromSign.length() < 15 ? playerNameFromSign
                : lowerCaseStartOfNameToCorrectNames.get(playerNameFromSign.toLowerCase());
    }

    /**
     * It seems the OfflinePlayer#getLastPlayed in Bukkit is broken.
     * It occasionally returns invalid values. Therefore we use this instead.
     * The playerName must be the full name but is not case sensitive.
     */
    /* TODO: Was this fixed?
    public static long getLastPlayed(String playerName) {
        String playerNameCC = fixPlayerNameCase(playerName);
        if (playerNameCC == null) return 0;

        Player player = Bukkit.getPlayerExact(playerNameCC);
        if (player != null && player.isOnline()) return System.currentTimeMillis();

        File playerFile = new File(playerfolder, playerNameCC+".dat");
        return playerFile.lastModified();
    }*/
    
    // -------------------------------------------- //
    // INTERNAL METHODS
    // -------------------------------------------- //
    
    protected synchronized static void populateCaseInsensitiveNameToCaseCorrectName()
    {
        // We can't use the files here anymore because they were changed to UUIDs with Minecraft 1.7.6+ :/
        // This way is definetly slower, but there is no better way currently
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            checkPlayer(player);
        }
    }

    protected static void checkPlayer(OfflinePlayer player) {
        String name = player.getName();
        if (name.length() >= 15) {
            String start = name.substring(0, 15).toLowerCase();
            if (!player.isOnline()) {
                String otherPlayer = lowerCaseStartOfNameToCorrectNames.get(start);
                // We can't know which player is the correct one, so we just use the one that has last played
                if (otherPlayer == null || UUIDs.getPlayer(otherPlayer).getLastPlayed() < player.getLastPlayed())
                    lowerCaseStartOfNameToCorrectNames.put(start, player.getName());
            } else
                lowerCaseStartOfNameToCorrectNames.put(start, player.getName());
        }
    }
}
