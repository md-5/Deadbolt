
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import com.daemitus.deadbolt.listener.DeadboltListener;
import java.util.Arrays;
import java.util.List;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.event.player.PlayerInteractEvent;

public class VaultListener extends DeadboltListener {

    protected static final String patternBracketTooLong = "\\[.{14,}\\]";
    Permission permissions;

    @Override
    public List<String> getDependencies() {
        return Arrays.asList("Vault");
    }

    @Override
    public void load(Deadbolt plugin) {
        permissions = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class).getProvider();
    }

    @Override
    public boolean canPlayerInteract(Deadbolted db, PlayerInteractEvent event) {
        Permission permission = null;
        String[] groups = permission.getPlayerGroups(event.getPlayer());
        for (String group : groups)
            if (db.getUsers().contains(truncate("[" + group + "]").toLowerCase()))
                return true;
        return false;
    }

    private String truncate(String text) {
        if (text.matches(patternBracketTooLong))
            return "[" + text.substring(1, 14) + "]";
        return text;
    }
}
