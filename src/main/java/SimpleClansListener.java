
import com.daemitus.deadbolt.listener.DeadboltListener;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import java.util.Arrays;
import java.util.List;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public final class SimpleClansListener extends DeadboltListener {

    private static final String patternBracketTooLong = "\\[.{14,}\\]";
    private SimpleClans sc;

    @Override
    public List<String> getDependencies() {
        return Arrays.asList("SimpleClans");
    }

    @Override
    public void load(final Deadbolt plugin) {
        sc = SimpleClans.getInstance();
    }

    @Override
    public boolean canPlayerInteract(Deadbolted db, PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ClanPlayer cp = sc.getClanManager().getClanPlayer(player);
        if (cp != null) {
            Clan clan = cp.getClan();
            if (clan != null) {
                if (db.getUsers().contains(truncate("[" + clan.getName() + "]")))
                    return true;
                if (db.getUsers().contains(truncate("[" + clan.getTag() + "]")))
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
