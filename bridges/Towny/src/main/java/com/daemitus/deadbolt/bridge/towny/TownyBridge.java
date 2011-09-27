package com.daemitus.deadbolt.bridge.towny;

import com.daemitus.deadbolt.Conf;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.bridge.DeadboltBridge;
import com.palmergames.bukkit.towny.NotRegisteredException;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyWorld;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

public class TownyBridge extends JavaPlugin implements DeadboltBridge {

    private final String REPO = "https://raw.github.com/daemitus/Deadbolt/master/bridges/Towny/src/main/resources/files/";
    private static final String patternBracketTooLong = "\\[.{14,}\\]";
    private static Towny towny;
    private static boolean denyWilderness = false;
    private static boolean wildernessOverride = false;
    private static boolean mayorOverride = false;
    private static boolean assistantOverride = false;

    @Override
    public void onDisable() {
        if (Deadbolt.unregisterBridge(this)) {
            Bukkit.getLogger().log(Level.INFO, "Deadbolt-Towny: disabled");
        } else {
            Bukkit.getLogger().log(Level.WARNING, "Deadbolt-Towny: Could not unregister with Deadbolt");
        }
    }

    @Override
    public void onEnable() {
        towny = (Towny) this.getServer().getPluginManager().getPlugin("Towny");
        if (towny == null) {
            Bukkit.getLogger().log(Level.WARNING, "Deadbolt-Towny: Towny not found");
        } else {
            if (Deadbolt.registerBridge(this)) {
                Bukkit.getLogger().log(Level.INFO, "Deadbolt-Towny: enabled");
                loadConfig();
            } else {
                Bukkit.getLogger().log(Level.WARNING, "Deadbolt-Towny: Could not register with Deadbolt");
            }
        }
    }

    @Override
    public boolean isOwner(Player player, Block block) {
        for (TownyWorld tw : towny.getTownyUniverse().getWorlds()) {
            if (tw.getName().equalsIgnoreCase(player.getWorld().getName()) && tw.isUsingTowny()) {
                if (wildernessOverride && towny.getTownyUniverse().isWilderness(block))
                    return true;
                if (mayorOverride) {
                    try {
                        Resident resident = towny.getTownyUniverse().getResident(player.getName());
                        Town blockTown = towny.getTownyUniverse().getTownBlock(block.getLocation()).getTown();
                        return blockTown.getMayor().equals(resident);
                    } catch (NotRegisteredException ex) {
                    }
                }
                if (assistantOverride) {
                    try {
                        Resident resident = towny.getTownyUniverse().getResident(player.getName());
                        Town blockTown = towny.getTownyUniverse().getTownBlock(block.getLocation()).getTown();
                        return blockTown.getAssistants().contains(resident);
                    } catch (NotRegisteredException ex) {
                    }
                }
                break;
            }
        }
        return false;
    }

    @Override
    public boolean isAuthorized(Player player, List<String> names) {
        try {
            Resident resident = towny.getTownyUniverse().getResident(player.getName());
            Town town = resident.getTown();
            if (names.contains(truncate("[" + town.getName().toLowerCase() + "]")))//town check
                return true;
            if (names.contains(truncate("+" + town.getName().toLowerCase() + "+"))
                    && (town.getMayor().equals(resident) || town.getAssistants().contains(resident)))//town assistant check
                return true;
            Nation nation = town.getNation();
            if (names.contains(truncate("[" + nation.getName() + "]").toLowerCase()))//nation check
                return true;
            if (names.contains(truncate("+" + nation.getName().toLowerCase() + "+"))
                    && (nation.getCapital().getMayor().equals(resident) || nation.getAssistants().contains(resident)))//nation assistant check
                return true;
        } catch (NotRegisteredException ex) {
        }
        return false;
    }

    @Override
    public boolean canProtect(Player player, Block block) {
        for (TownyWorld tw : towny.getTownyUniverse().getWorlds()) {
            if (tw.getName().equalsIgnoreCase(player.getWorld().getName()) && tw.isUsingTowny()) {
                if (denyWilderness && towny.getTownyUniverse().isWilderness(block)) {
                    Conf.sendMessage(player, "You can only protect blocks inside of a town", ChatColor.RED);
                    return false;
                }
                break;
            }
        }
        return true;
    }

    private String truncate(String text) {
        if (text.matches(patternBracketTooLong))
            return "[" + text.substring(1, 14) + "]";
        return text;
    }

    private void loadConfig() {
        File configFile = new File(getDataFolder() + File.separator + "config.yml");
        if (!configFile.exists())
            downloadFile("config.yml");
        Configuration config = new Configuration(configFile);
        config.load();
        denyWilderness = config.getBoolean("deny_wilderness", denyWilderness);
        wildernessOverride = config.getBoolean("wilderness_override", wildernessOverride);
        mayorOverride = config.getBoolean("mayor_override", mayorOverride);
        assistantOverride = config.getBoolean("assistant_override", assistantOverride);
    }

    private void downloadFile(String filename) {
        //Southpaw018 - Cenotaph
        if (!getDataFolder().exists())
            getDataFolder().mkdirs();
        String datafile = getDataFolder().getPath() + File.separator + filename;
        String repofile = REPO + filename;
        File download = new File(datafile);
        try {
            download.createNewFile();
            URL link = new URL(repofile);
            ReadableByteChannel rbc = Channels.newChannel(link.openStream());
            FileOutputStream fos = new FileOutputStream(download);
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            Bukkit.getLogger().log(Level.INFO, "Deadbolt: Downloaded file ".concat(datafile));
        } catch (MalformedURLException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Deadbolt: Malformed URL ".concat(repofile));
            download.delete();
        } catch (FileNotFoundException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Deadbolt: File not found ".concat(datafile));
            download.delete();
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Deadbolt: IOError downloading ".concat(repofile));
            download.delete();
        }
    }
}