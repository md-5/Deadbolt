package com.daemitus.deadbolt.bridge;

import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface DeadboltBridge {

    /**
     * Verifies that a player is authorized to use a protected block.
     * Default return should be false if not used.
     * @param player The player to be checked
     * @param names A List<String> of lines on associated signs in lower case
     * @return
     */
    public boolean isAuthorized(Player player, List<String> names);

    /**
     * Verifies that a player is allowed to protect blocks
     * Default return should be true if not used.
     * This should include a reason for denial to the player
     * @param player The player to be checked
     * @param block The sign block being placed
     * @return
     */
    public boolean canProtect(Player player, Block block);
}
