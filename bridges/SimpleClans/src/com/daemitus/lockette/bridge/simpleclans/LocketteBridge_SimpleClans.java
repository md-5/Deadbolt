package com.daemitus.lockette.bridge.simpleclans;

import com.daemitus.lockette.Lockette;
import com.daemitus.lockette.bridge.LocketteBridge;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class LocketteBridge_SimpleClans extends JavaPlugin implements LocketteBridge {

    public static final Logger logger = Logger.getLogger("Minecraft");
    private static final String patternBracketTooLong = "\\[.{14,}\\]";
    private SimpleClans sc;

    public void onDisable() {
    }

    public void onEnable() {
        sc = (SimpleClans) this.getServer().getPluginManager().getPlugin("SimpleClans");
        if (sc == null) {
            logger.log(Level.WARNING, "LocketteBridge_SimpleClans: SimpleClans not found");
        } else {
            if (Lockette.registerBridge(this)) {
                logger.log(Level.INFO, "LocketteBridge_SimpleClans: enabled");
            } else {
                logger.log(Level.WARNING, "LocketteBridge_SimpleClans: Could not register with Lockette");
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
            }
        }
        return false;
    }

    private String truncate(String text) {
        if (text.matches(patternBracketTooLong))
            return "[" + text.substring(1, 14) + "]";
        return text;
    }
}
