package com.daemitus.deadbolt.bridge.simpleclans;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.bridge.DeadboltBridge;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleClansBridge extends JavaPlugin implements DeadboltBridge {

    public static final Logger logger = Bukkit.getServer().getLogger();
    private static final String patternBracketTooLong = "\\[.{14,}\\]";
    private SimpleClans sc;

    public void onDisable() {
        if (Deadbolt.unregisterBridge(this)) {
            logger.log(Level.INFO, "Deadbolt-SimpleClans: disabled");
        } else {
            logger.log(Level.WARNING, "Deadbolt-SimpleClans: Could not unregister with Deadbolt");
        }
    }

    public void onEnable() {
        sc = (SimpleClans) this.getServer().getPluginManager().getPlugin("SimpleClans");
        if (sc == null) {
            logger.log(Level.WARNING, "Deadbolt-SimpleClans: SimpleClans not found");
        } else {
            if (Deadbolt.registerBridge(this)) {
                logger.log(Level.INFO, "Deadbolt-SimpleClans: enabled");
            } else {
                logger.log(Level.WARNING, "Deadbolt-SimpleClans: Could not register with Deadbolt");
            }
        }
    }

    public boolean isAuthorized(Player player, List<String> names) {
        ClanPlayer cp = sc.getClanManager().getClanPlayer(player);
        if (cp != null) {
            Clan clan = cp.getClan();
            if (clan != null) {
                if (names.contains(truncate("[" + clan.getName() + "]").toLowerCase()))
                    return true;
                if (names.contains(truncate("[" + clan.getTag() + "]").toLowerCase()))
                    return true;
            }
        }
        return false;
    }

    public boolean canProtect(Player player, Block block) {
        //Not used
        return true;
    }

    private String truncate(String text) {
        if (text.matches(patternBracketTooLong))
            return "[" + text.substring(1, 14) + "]";
        return text;
    }
}
