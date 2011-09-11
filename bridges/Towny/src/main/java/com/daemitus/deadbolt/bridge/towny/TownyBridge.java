package com.daemitus.deadbolt.bridge.towny;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Util;
import com.daemitus.deadbolt.bridge.DeadboltBridge;
import com.palmergames.bukkit.towny.NotRegisteredException;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TownyBridge extends JavaPlugin implements DeadboltBridge {

    public static final Logger logger = Bukkit.getServer().getLogger();
    private static final String patternBracketTooLong = "\\[.{14,}\\]";
    private static final String denyall_wilderness = "deadbolt.towny.wild.denyall";
    private static Towny towny;

    public void onDisable() {
        if (Deadbolt.unregisterBridge(this)) {
            logger.log(Level.INFO, "Deadbolt-Towny: disabled");
        } else {
            logger.log(Level.WARNING, "Deadbolt-Towny: Could not unregister with Deadbolt");
        }
    }

    public void onEnable() {
        towny = (Towny) this.getServer().getPluginManager().getPlugin("Towny");
        if (towny == null) {
            logger.log(Level.WARNING, "Deadbolt-Towny: Towny not found");
        } else {
            if (Deadbolt.registerBridge(this)) {
                logger.log(Level.INFO, "Deadbolt-Towny: enabled");
            } else {
                logger.log(Level.WARNING, "Deadbolt-Towny: Could not register with Deadbolt");
            }
        }
    }

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

    public boolean canProtect(Player player, Block block) {
        if (towny.getTownyUniverse().isWilderness(block) && player.hasPermission(denyall_wilderness)) {
            Util.sendMessage(player, "You can only protect blocks inside of a town", ChatColor.RED);
            return false;
        }
        return true;
    }

    private String truncate(String text) {
        if (text.matches(patternBracketTooLong))
            return "[" + text.substring(1, 14) + "]";
        return text;
    }
}
