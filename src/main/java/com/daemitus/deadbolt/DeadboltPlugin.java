package com.daemitus.deadbolt;

import com.daemitus.deadbolt.events.*;
import com.md_5.config.FileYamlStorage;
import java.io.File;
import java.util.regex.Pattern;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class DeadboltPlugin extends JavaPlugin implements Listener {

    public Config c;
    public Language l;
    private FileYamlStorage<Config> configStorage;
    private FileYamlStorage<Language> languageStorage;

    @Override
    public void onEnable() {
        Deadbolt.setPlugin(this);
        bootStrap();

        new PlayerNameUtil(this);

        new BlockListener();
        new PlayerListener();
        new SignListener();

        if (Deadbolt.getConfig().deny_endermen) {
            new EndermanListener();
        }
        if (Deadbolt.getConfig().deny_explosions) {
            new ExplosionListener();
        }
        if (Deadbolt.getConfig().deny_entity_interact) {
            new EntityInteractListener();
        }
        if (Deadbolt.getConfig().deny_pistons) {
            new PistonListener();
        }
        if (Deadbolt.getConfig().deny_redstone) {
            new RedstoneListener();
        }
        if (Deadbolt.getConfig().deny_hoppercart) {
            new HopperMinecartListener();
        }


        getServer().getPluginManager().registerEvents(this, this);
        getCommand("deadbolt").setExecutor(new DeadboltCommandExecutor(this));
    }

    @Override
    public void onDisable() {
        ToggleDoorTask.cleanup();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Deadbolt.getConfig().selectedSign.remove(event.getPlayer());
    }

    public void bootStrap() {
        configStorage = new FileYamlStorage<Config>(new File(getDataFolder(), "config.yml"), Config.class, this);
        c = configStorage.load();
        configStorage.save();

        File langFile = new File(getDataFolder(), c.language);
        if (!langFile.exists()) {
            Deadbolt.getLogger().warning(langFile.getName() + " not found, copying default english.yml");
            langFile = new File(getDataFolder(), "english.yml");
        }
        languageStorage = new FileYamlStorage<Language>(langFile, Language.class, this);
        l = languageStorage.load();
        languageStorage.save();

        if (l.signtext_private.length() > 13) {
            Deadbolt.getLogger().warning(l.signtext_private + " is too long, defaulting to [" + (l.signtext_private = l.d_signtext_private) + "]");
        }
        l.p_signtext_private = Pattern.compile("\\[(?i)(" + l.d_signtext_private + "|" + l.signtext_private + ")\\]");
        l.signtext_private = "[" + l.signtext_private + "]";

        if (l.signtext_moreusers.length() > 13) {
            Deadbolt.getLogger().warning(l.signtext_moreusers + " is too long, defaulting to [" + (l.signtext_moreusers = l.d_signtext_moreusers) + "]");
        }
        l.p_signtext_moreusers = Pattern.compile("\\[(?i)(" + l.d_signtext_moreusers + "|" + l.signtext_moreusers + ")\\]");
        l.signtext_moreusers = "[" + l.signtext_moreusers + "]";

        if (l.signtext_everyone.length() > 13) {
            Deadbolt.getLogger().warning(l.signtext_everyone + " is too long, defaulting to [" + (l.signtext_everyone = l.d_signtext_everyone) + "]");
        }
        l.p_signtext_everyone = Pattern.compile("\\[(?i)(" + l.d_signtext_everyone + "|" + l.signtext_everyone + ")\\]");

        if (l.signtext_timer.length() > 13) {
            Deadbolt.getLogger().warning(l.signtext_timer + " is too long, defaulting to [" + (l.signtext_timer = l.d_signtext_timer) + ":#]");
        }
        l.p_signtext_timer = Pattern.compile("\\[(?i)(" + l.d_signtext_timer + "|" + l.signtext_timer + "):\\s*([0-9]+)\\]");
    }
}
