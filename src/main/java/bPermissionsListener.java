
import com.daemitus.deadbolt.DeadboltPlugin;
import com.daemitus.deadbolt.Deadbolted;
import com.daemitus.deadbolt.listener.DeadboltListener;
import com.daemitus.deadbolt.Util;
import de.bananaco.permissions.Permissions;
import de.bananaco.permissions.worlds.WorldPermissionsManager;
import java.util.Arrays;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public final class bPermissionsListener extends DeadboltListener {

    private WorldPermissionsManager permissions;

    @Override
    public List<String> getDependencies() {
        return Arrays.asList("bPermissions");
    }

    @Override
    public void load(final DeadboltPlugin plugin) {
        permissions = Permissions.getWorldPermissionsManager();
    }

    @Override
    public boolean canPlayerInteract(Deadbolted db, PlayerInteractEvent event) {
        Player player = event.getPlayer();
        List<String> groups = permissions.getPermissionSet(player.getLocation().getWorld()).getGroups(player);
        for (String gName : groups) {
            if (db.getUsers().contains(Util.truncate("[" + gName + "]"))) {
                return true;
            }
        }
        return false;
    }
}
