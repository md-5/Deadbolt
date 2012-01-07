
import com.daemitus.deadbolt.listener.DeadboltListener;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public final class PermissionsExListener extends DeadboltListener {

    private PermissionManager permissions;
    private static final String patternBracketTooLong = "\\[.{14,}\\]";

    @Override
    public List<String> getDependencies() {
        return Arrays.asList("PermissionsEx");
    }

    @Override
    public void load(final Deadbolt plugin) {
        permissions = PermissionsEx.getPermissionManager();
    }

    @Override
    public boolean canPlayerInteract(Deadbolted db, PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Set<String> allGroupNames = new HashSet<String>();
        for (PermissionGroup g : permissions.getUser(player.getName()).getGroups()) {
            allGroupNames.add(g.getName());
            getInherited(g, allGroupNames);
        }

        for (String group : allGroupNames) {
            if (db.getUsers().contains(truncate("[" + group + "]").toLowerCase()))
                return true;
        }
        return false;
    }

    private void getInherited(PermissionGroup group, Set<String> groupNames) {
        for (PermissionGroup g : group.getParentGroups()) {
            if (groupNames.add(g.getName())) {
                getInherited(g, groupNames);
            }
        }
    }

    private String truncate(String text) {
        if (text.matches(patternBracketTooLong))
            return "[" + text.substring(1, 14) + "]";
        return text;
    }
}
