
import com.daemitus.deadbolt.DeadboltPlugin;
import com.daemitus.deadbolt.Deadbolted;
import com.daemitus.deadbolt.listener.DeadboltListener;
import com.daemitus.deadbolt.Util;
import com.platymuus.bukkit.permissions.Group;
import com.platymuus.bukkit.permissions.PermissionsPlugin;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public final class PermissionsBukkitListener extends DeadboltListener {

    private PermissionsPlugin permissions;

    @Override
    public List<String> getDependencies() {
        return Arrays.asList("PermissionsBukkit");
    }

    @Override
    public void load(final DeadboltPlugin plugin) {
        permissions = (PermissionsPlugin) plugin.getServer().getPluginManager().getPlugin("PermissionsBukkit");
    }

    @Override
    public boolean canPlayerInteract(Deadbolted db, PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Set<String> allGroupNames = new HashSet<String>();
        for (Group g : permissions.getGroups(player.getName())) {
            allGroupNames.add(g.getName());
            getInherited(g, allGroupNames);
        }

        for (String group : allGroupNames) {
            if (db.getUsers().contains(Util.truncate("[" + group + "]"))) {
                return true;
            }
        }

        return false;
    }

    private void getInherited(Group group, Set<String> groupNames) {
        for (Group g : group.getInfo().getGroups()) {
            if (groupNames.add(g.getName())) {
                getInherited(g, groupNames);
            }
        }
    }
}
