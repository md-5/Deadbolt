
import com.daemitus.deadbolt.listener.BaseListener;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class SimpleClansListener extends BaseListener {

    private static final String patternBracketTooLong = "\\[.{14,}\\]";
    private SimpleClans sc;

    @Override
    public void load(Deadbolt plugin) {
        sc = SimpleClans.getInstance();
    }

    @Override
    public boolean canPlayerInteract(Deadbolted db, PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ClanPlayer cp = sc.getClanManager().getClanPlayer(player);
        if (cp != null) {
            Clan clan = cp.getClan();
            if (clan != null) {
                if (db.getUsers().contains(truncate("[" + clan.getName() + "]").toLowerCase()))
                    return true;
                if (db.getUsers().contains(truncate("[" + clan.getTag() + "]").toLowerCase()))
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
