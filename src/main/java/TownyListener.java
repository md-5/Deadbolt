
import com.daemitus.deadbolt.listener.DeadboltListener;
import com.daemitus.deadbolt.Config;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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

public final class TownyListener extends DeadboltListener {

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
        try {
            towny = TownyUniverse.plugin.getTownyUniverse();

            File configFile = new File(plugin.getDataFolder() + "/listeners/TownyListener.yml");
            checkFile(configFile);
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

    private boolean checkFile(File file) {
        try {
            if (!file.exists())
                file.createNewFile();
            FileWriter fw = new FileWriter(file);
            fw.write("deny_wilderness:      false  #Denies protecting of blocks in the wild\n");
            fw.write("wilderness_override:  false  #Allows ANYONE to break open locks in the wilderness\n");
            fw.write("mayor_override:       false  #Allows Mayors to break open locks in their town\n");
            fw.write("assistant_override:   false  #Allows Assistants to break open locks in their town\n");
            fw.close();
            Deadbolt.logger.log(Level.INFO, "[Deadbolt][" + this.getClass().getSimpleName() + "] Retrieved file " + file.getName());
            return true;
        } catch (IOException ex) {
            Deadbolt.logger.log(Level.SEVERE, "[Deadbolt][" + this.getClass().getSimpleName() + "] Error retrieving " + file.getName());
            return false;
        }
    }

    @Override
    public boolean canPlayerInteract(Deadbolted db, PlayerInteractEvent event) {
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
        return super.canPlayerInteract(db, event);
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
                        TownBlock townBlock = towny.getTownBlock(block.getLocation());
                        TownyPermission plotPerms = townBlock.getPermissions();
                        Town town = townBlock.getTown();
                        TownyPermission townPerms = town.getPermissions();
                                            //todo rework plot permissions in                          
                        if (resident.hasTown() && resident.getTown().equals(town) && townPerms.residentBuild)
                            return true;
                        if (resident.hasNation() && resident.getTown().getNation().equals(town.getNation()) && townPerms.allyBuild)
                            return true;
                        if (townPerms.outsiderBuild)
                            return true;
                        Deadbolt.logger.warning("d");
                        return false;

                    } catch (Exception ex) {
                    }
                }
                break;
            }
        }
        return super.canSignChange(db, event);
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
        return super.canSignChangeQuick(db, event);
    }
}
