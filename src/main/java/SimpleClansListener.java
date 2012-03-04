
import com.daemitus.deadbolt.DeadboltPlugin;
import com.daemitus.deadbolt.Deadbolted;
import com.daemitus.deadbolt.listener.DeadboltListener;
import com.daemitus.deadbolt.Util;
import java.util.Arrays;
import java.util.List;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public final class SimpleClansListener extends DeadboltListener {

    private SimpleClans sc;

    @Override
    public List<String> getDependencies() {
        return Arrays.asList("SimpleClans");
    }

    @Override
    public void load(final DeadboltPlugin plugin) {
        sc = SimpleClans.getInstance();
    }

    @Override
    public boolean canPlayerInteract(Deadbolted db, PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ClanPlayer cp = sc.getClanManager().getClanPlayer(player);
        if (cp != null) {
            Clan clan = cp.getClan();
            if (clan != null) {
                if (db.getUsers().contains(Util.truncate("[" + clan.getName() + "]"))) {
                    return true;
                }
                if (db.getUsers().contains(Util.truncate("[" + clan.getTag() + "]"))) {
                    return true;
                }
            }
        }
        return false;
    }
}
