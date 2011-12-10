
import com.daemitus.deadbolt.listener.DeadboltListener;
import com.daemitus.deadbolt.Config;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
//import com.palmergames.bukkit.towny.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class TownyListener extends DeadboltListener {

    private Deadbolt plugin;
    private static TownyUniverse towny;
    protected static final String patternBracketTooLong = "\\[.{14,}\\]";
    private static boolean denyWilderness = false;
    private static boolean wildernessOverride = false;
    private static boolean mayorOverride = false;
    private static boolean assistantOverride = false;

    @Override
    public List<String> getDependencies() {
        return Arrays.asList("Towny");
    }

    @Override
    public void load(final Deadbolt plugin) {
        this.plugin = plugin;
        towny = TownyUniverse.plugin.getTownyUniverse();

        try {
            File configFile = new File(plugin.getDataFolder() + "/listeners/Towny/config.yml");
            if (!configFile.exists())
                getFile("config.yml");
            YamlConfiguration config = new YamlConfiguration();
            config.load(configFile);
            denyWilderness = config.getBoolean("deny_wilderness", denyWilderness);
            wildernessOverride = config.getBoolean("wilderness_override", wildernessOverride);
            mayorOverride = config.getBoolean("mayor_override", mayorOverride);
            assistantOverride = config.getBoolean("assistant_override", assistantOverride);
        } catch (FileNotFoundException ex) {
            Deadbolt.logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Deadbolt.logger.log(Level.SEVERE, null, ex);
        } catch (InvalidConfigurationException ex) {
            Deadbolt.logger.log(Level.SEVERE, null, ex);
        }
    }

    private String truncate(String text) {
        if (text.matches(patternBracketTooLong))
            return "[" + text.substring(1, 14) + "]";
        return text;
    }

    private boolean getFile(String filename) {
        try {//TODO dont save in jar, create programatically. just 4 lines really.
            File dir = new File(plugin.getDataFolder() + "/listeners/Towny");
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(plugin.getDataFolder().getAbsolutePath() + "/listeners/Towny/" + filename);
            file.createNewFile();

            InputStream fis = plugin.getResource("Towny/" + filename);
            FileOutputStream fos = new FileOutputStream(file);

            try {
                byte[] buf = new byte[1024];
                int i = 0;
                while ((i = fis.read(buf)) != -1) {
                    fos.write(buf, 0, i);
                }
            } catch (Exception e) {
                e.printStackTrace(System.out);
            } finally {
                if (fis != null) {
                    fis.close();
                }
                if (fos != null) {
                    fos.close();
                }
            }
            Deadbolt.logger.log(Level.INFO, String.format("Deadbolt-Towny: Retrieved file %1$s", filename));
            return true;
        } catch (IOException ex) {
            Deadbolt.logger.log(Level.SEVERE, String.format("Deadbolt-Towny: Error retrieving %1$s", filename));
            return false;
        }
    }

    @Override
    public boolean canPlayerInteract(Deadbolted db, PlayerInteractEvent event) {
        //DEFAULT return false;
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        try {
            for (TownyWorld tw : towny.getWorlds()) {
                if (tw.getName().equalsIgnoreCase(player.getWorld().getName()) && tw.isUsingTowny()) {
                    Resident resident = towny.getResident(player.getName());

                    Town town = resident.getTown();
                    if (db.getUsers().contains(truncate("[" + town.getName().toLowerCase() + "]")))//town check
                        return true;
                    if (db.getUsers().contains(truncate("+" + town.getName().toLowerCase() + "+"))
                            && (town.getMayor().equals(resident) || town.getAssistants().contains(resident)))//town assistant check
                        return true;

                    Nation nation = town.getNation();
                    if (db.getUsers().contains(truncate("[" + nation.getName() + "]").toLowerCase()))//nation check
                        return true;
                    if (db.getUsers().contains(truncate("+" + nation.getName().toLowerCase() + "+"))
                            && (nation.getCapital().getMayor().equals(resident) || nation.getAssistants().contains(resident)))//nation assistant check
                        return true;

                    if (wildernessOverride && towny.isWilderness(block))
                        return true;
                    if (mayorOverride)
                        return towny.getTownBlock(block.getLocation()).getTown().getMayor().equals(resident);
                    if (assistantOverride)
                        return towny.getTownBlock(block.getLocation()).getTown().getAssistants().contains(resident);
                    break;
                }
            }
        } catch (Exception ex) {
        }
        return false;
    }

    @Override
    public boolean canSignChange(Deadbolted db, SignChangeEvent event) {
        //DEFAULT return true;
        Player player = event.getPlayer();
        Block block = event.getBlock();
        for (TownyWorld tw : towny.getWorlds()) {
            if (tw.getName().equalsIgnoreCase(player.getWorld().getName()) && tw.isUsingTowny()) {
                if (towny.isWilderness(block)) {
                    if (denyWilderness) {
                        Config.sendMessage(player, ChatColor.RED, "You can only protect blocks inside of a town");
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    try {
                        Resident resident = towny.getResident(player.getName());
                        Town town = towny.getTownBlock(block.getLocation()).getTown();
                        TownyPermission permissions = town.getPermissions();
                        if (resident.hasTown() && resident.getTown().equals(town) && permissions.residentBuild)
                            return true;
                        if (resident.hasNation() && resident.getTown().getNation().equals(town.getNation()) && permissions.allyBuild)
                            return true;
                        if (permissions.outsiderBuild)
                            return true;
                        Deadbolt.logger.warning("d");
                        return false;

                    } catch (Exception ex) {
                    }
                }
                break;
            }
        }
        return true;
    }

    @Override
    public boolean canSignChangeQuick(Deadbolted db, PlayerInteractEvent event) {
        //DEFAULT return true;
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        for (TownyWorld tw : towny.getWorlds()) {
            if (tw.getName().equalsIgnoreCase(player.getWorld().getName()) && tw.isUsingTowny()) {
                if (towny.isWilderness(block)) {
                    if (denyWilderness) {
                        Config.sendMessage(player, ChatColor.RED, "You can only protect blocks inside of a town");
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    try {
                        Resident resident = towny.getResident(player.getName());
                        Town town = towny.getTownBlock(block.getLocation()).getTown();
                        TownyPermission permissions = town.getPermissions();
                        if (resident.hasTown() && resident.getTown().equals(town) && permissions.residentBuild)
                            return true;
                        if (resident.hasNation() && resident.getTown().getNation().equals(town.getNation()) && permissions.allyBuild)
                            return true;
                        if (permissions.outsiderBuild)
                            return true;
                        Deadbolt.logger.warning("d");
                        return false;

                    } catch (Exception ex) {
                    }
                }
                break;
            }
        }
        return true;
    }
}
