
import com.daemitus.deadbolt.listener.DeadboltListener;
import com.daemitus.deadbolt.Deadbolted;
import de.bananaco.bpermissions.api.WorldManager;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public final class bPermissionsListener extends DeadboltListener {

    private final WorldManager permissions = WorldManager.getInstance();
    protected static final String patternBracketTooLong = "\\[.{14,}\\]";

    @Override
    public List<String> getDependencies() {
        return Arrays.asList("bPermissions");
    }


    @Override
    public boolean canPlayerInteract(Deadbolted db, PlayerInteractEvent event) {
        //DEFAULT return false;
        Player player = event.getPlayer();
        Set<String> groups = permissions.getWorld(player.getWorld().getName()).getUser(player.getName()).getGroupsAsString();
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
