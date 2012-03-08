package com.daemitus.deadbolt;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.bukkit.Bukkit;
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
	
	// -------------------------------------------- //
	// INTERNAL METHODS
	// -------------------------------------------- //
	
	protected synchronized static void populateCaseInsensitiveNameToCaseCorrectName()
	{
		// Find the player folder
		File playerfolder = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "players");
		
		// Populate by removing .dat
		for (File playerfile : playerfolder.listFiles())
		{
			String filename = playerfile.getName();
			String playername = filename.substring(0, filename.length()-4);
			nameToCorrectName.put(playername, playername);
		}
	}
	
}
