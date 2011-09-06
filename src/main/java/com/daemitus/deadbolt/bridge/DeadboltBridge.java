package com.daemitus.deadbolt.bridge;

import java.util.List;
import org.bukkit.entity.Player;

public interface DeadboltBridge {

    public boolean isAuthorized(Player player, List<String> names);
}
