package com.daemitus.lockette;

import java.io.File;
import org.bukkit.util.config.Configuration;

public class ConfigManager {

    private final Lockette plugin;
    //------------------------------------------------------------------------//
    //------------------------------------------------------------------------//
    //------------------------------------------------------------------------//
    public static boolean setting_Timer_Doors_Always_On = false;
    public static int setting_Timer_Doors_Always_On_Delay = 3;
    public static boolean setting_Explosion_Protection = true;
    public static boolean setting_Broadcast_TNT_Fizzle = true;
    public static int setting_Broadcast_TNT_Fizzle_Radius = 25;
    //------------------------------------------------------------------------//
    private static Configuration config;
    private static Configuration defaults;
    private static Configuration locale;

    public ConfigManager(final Lockette plugin) {
        this.plugin = plugin;
    }

    public void load() {
        config = plugin.getConfiguration();
        config.load();
        loadMessages(config.getString("language"));
    }

    public void loadMessages(String language) {
        File defaultsFile = new File(plugin.getDataFolder() + File.separator + "defaults.yml");
        defaults = new Configuration(defaultsFile);
        defaults.setProperty("signtext-private", "[private]");
        defaults.setProperty("signtext-moreusers", "[more users]");
        defaults.setProperty("signtext-everyone", "[everyone]");
        defaults.setProperty("signtext-timer", "timer");
        defaults.setProperty("console", "Lockette: This command requires ingame usage");
        defaults.setProperty("cmd-sign-selected", "Sign selected, use /lockette <line number> <text>");
        defaults.setProperty("cmd-sign-not-selected", "Nothing selected, right click a valid sign first");
        defaults.setProperty("cmd-bad-format", "Bad format, use /lockette <line number> <text>");
        defaults.setProperty("cmd-identifier-not-changeable", "Break and replace to change the identifier on line 1");
        defaults.setProperty("cmd-owner-not-changeable", "Break and replace to change the owner on line 2");
        defaults.setProperty("cmd-line-num-out-of-range", "Bad format, your line number should be 2,3,4");
        defaults.setProperty("msg-deny-door", "Access denied");
        defaults.setProperty("msg-deny-container", "Access denied");
        defaults.setProperty("msg-deny-sign", "You don't own this sign");
        defaults.setProperty("msg-deny-block-break", "You don't own this block");
        defaults.setProperty("msg-deny-placement-chest", "You don't own the adjacent chest");
        defaults.setProperty("msg-deny-placement-door", "You don't own the adjacent door");
        defaults.setProperty("msg-deny-placement-trapdoor", "You don't own the adjacent hinge block");
        defaults.setProperty("msg-deny-placement-sign-nothing-nearby", "Nothing nearby to protect");
        defaults.setProperty("msg-deny-placement-sign-private-owner", "This block is already protected");
        defaults.setProperty("msg-deny-placement-sign-moreusers-owner", "You don't own this block");
        defaults.setProperty("msg-deny-placement-sign-moreusers-needs-private", "No sign with [Private] nearby");
        defaults.setProperty("msg-player-not-found-warning", "Player not found online, check your name");
        defaults.setProperty("msg-tnt-fizzle", "TNT tried to explode too close to a protected block");



        File langFile = new File(plugin.getDataFolder() + File.separator + (language == null ? "defaults" : language) + ".yml");
        locale = new Configuration(langFile);
        boolean save = false;
        for (String key : defaults.getKeys()) {
            if (locale.getString(key) == null) {
                locale.setProperty(key, defaults.getString(key));
                save = true;
            }
        }
        if (save)
            locale.save();
    }

    public static String getDefault(String key) {
        return defaults.getString(key);
    }

    public static String getLocale(String key) {
        return locale.getString(key);
    }
}
