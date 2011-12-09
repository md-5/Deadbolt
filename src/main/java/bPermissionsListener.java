
import com.daemitus.deadbolt.listener.BaseListener;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import de.bananaco.permissions.Permissions;
import de.bananaco.permissions.worlds.WorldPermissionsManager;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class bPermissionsListener extends BaseListener {

    private WorldPermissionsManager permissions;
    protected static final String patternBracketTooLong = "\\[.{14,}\\]";

    @Override
    public void load(final Deadbolt plugin) {
        permissions = Permissions.getWorldPermissionsManager();

    }

    @Override
    public boolean canPlayerInteract(Deadbolted db, PlayerInteractEvent event) {
        //DEFAULT return false;
        Player player = event.getPlayer();
        List<String> groups = permissions.getPermissionSet(player.getLocation().getWorld()).getGroups(player);
        for (String gName : groups) {
            if (db.getUsers().contains(truncate("[" + gName + "]").toLowerCase()))
                return true;
        }
        return false;
    }

    private String truncate(String text) {
        if (text.matches(patternBracketTooLong))
            return "[" + text.substring(1, 14) + "]";
        return text;
    }
}
