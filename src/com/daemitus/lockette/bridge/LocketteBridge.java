package com.daemitus.lockette.bridge;

import java.util.List;
import org.bukkit.entity.Player;

public interface LocketteBridge {

    public boolean isAuthorized(Player player, List<String> names);
}
