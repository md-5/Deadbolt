package com.daemitus.deadbolt;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPreLoginEvent;
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
     * We will use this folder later. 
     */
    public static File playerfolder = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "players");
    
    /**
     * This map is populated using the player.dat files on disk.
     * It is also populated when a player tries to log in to the server.
     */
    protected static Map<String, String> nameToCorrectName = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
    
    /**
     * This map is used to improve the speed of name start lookups.
     * Note that the keys in this map is lowercase.
     */
    protected static Map<String, Set<String>> lowerCaseStartOfNameToCorrectNames = new TreeMap<String, Set<String>>();
    
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
        if ( ! isPopulated) {
            populateCaseInsensitiveNameToCaseCorrectName();
            isPopulated = true;
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public synchronized void onLowestPlayerPreLoginEvent(PlayerPreLoginEvent event)
    {
        String newPlayerName = event.getName();
        String lowercaseNewPlayerName = newPlayerName.toLowerCase();
        
        // Add this name to the case-corrector map
        nameToCorrectName.put(newPlayerName, newPlayerName);
        
        // Update the cache
        for (Entry<String, Set<String>> entry : lowerCaseStartOfNameToCorrectNames.entrySet()) {
            if (lowercaseNewPlayerName.startsWith(entry.getKey())) {
                entry.getValue().add(newPlayerName);
            }
        }
    }
    
    // -------------------------------------------- //
    // PUBLIC METHODS
    // -------------------------------------------- //
    
    /**
     * This method simply checks if the playerName is a valid one.
     * Mojangs rules for Minecraft character registration is used.
     */
    public static boolean isValidPlayerName(final String playerName) {
        return Pattern.matches("^[a-zA-Z0-9_]{2,16}$", playerName);
    }
    
    /**
     * This method takes a player name and returns the same name but with correct case.
     * Null is returned if the correct case can not be determined.
     */
    public static String fixPlayerNameCase(final String playerName) {
        return nameToCorrectName.get(playerName);
    }
    
    /**
     * Find all player names starting with a certain string (not case sensitive).
     * This method will return the names of offline players as well as online players.
     */
    public synchronized static Set<String> getAllPlayerNamesCaseinsensitivelyStartingWith(final String startOfName) {
        Set<String> ret = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        
        String lowercaseStartOfName = startOfName.toLowerCase();
        
        // Try to fetch from the cache
        Set<String> cachedNames = lowerCaseStartOfNameToCorrectNames.get(lowercaseStartOfName);
        if (cachedNames != null) {
            ret.addAll(cachedNames);
            return ret;
        }
        
        // Build it the hard way if cache did not exist
        
        ret = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        for (String correctName : nameToCorrectName.values()) {
            if (correctName.toLowerCase().startsWith(lowercaseStartOfName)) {
                ret.add(correctName);
            }
        }
        
        // Add it to the cache
        Set<String> shallowCopyForCache = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        shallowCopyForCache.addAll(ret);
        lowerCaseStartOfNameToCorrectNames.put(lowercaseStartOfName, shallowCopyForCache);
        
        return ret;
    }
    
    /**
     * In Minecraft a playername can be 16 characters long. One sign line is however only 15 characters long.
     * If we find a 15 character long playername on a sign it could thus refer to more than one player.
     * This method finds all possible matching player names.
     */
    public static Set<String> interpretPlayerNameFromSign(String playerNameFromSign) {
        Set<String> ret = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        
        if (playerNameFromSign.length() > 15) {
            // This case will in reality not happen.
            ret.add(playerNameFromSign);
            return ret;
        }
        
        if (playerNameFromSign.length() == 15) {
            ret.addAll(PlayerNameUtil.getAllPlayerNamesCaseinsensitivelyStartingWith(playerNameFromSign));
        } else {
            String fixedPlayerName = PlayerNameUtil.fixPlayerNameCase(playerNameFromSign);
            if (fixedPlayerName != null) {
                ret.add(fixedPlayerName);
            }
        }
        
        return ret;
    }
    
    /**
     * It seems the OfflinePlayer#getLastPlayed in Bukkit is broken.
     * It occasionally returns invalid values. Therefore we use this instead.
     * The playerName must be the full name but is not case sensitive. 
     */
    public static long getLastPlayed(String playerName) {
	String playerNameCC = fixPlayerNameCase(playerName);
	if (playerNameCC == null) return 0;
		
	Player player = Bukkit.getPlayerExact(playerNameCC);
	if (player != null && player.isOnline()) return System.currentTimeMillis();
	
	File playerFile = new File(playerfolder, playerNameCC+".dat");
	return playerFile.lastModified();
    }
    
    // -------------------------------------------- //
    // INTERNAL METHODS
    // -------------------------------------------- //
    
    protected synchronized static void populateCaseInsensitiveNameToCaseCorrectName()
    {   
        // Populate by removing .dat
        for (File playerfile : playerfolder.listFiles())
        {
            String filename = playerfile.getName();
            String playername = filename.substring(0, filename.length()-4);
            nameToCorrectName.put(playername, playername);
        }
    }
}
