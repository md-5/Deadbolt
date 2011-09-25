package com.daemitus.deadbolt.bridge;

import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface DeadboltBridge {

    /**
     * Overrides default isOwner logic for the purposes of breaking. Use isAuthorized for interacting or canProtect for protecting. False if unused.
     * @param player The player to be checked
     * @param block The block checked against
     * @return 
     */
    public boolean isOwner(Player player, Block block);

    /**
     * Verifies that a player is authorized to use a protected block. False if unused.
     * @param player The player to be checked
     * @param names A List<String> of lines on associated signs in lower case
     * @return
     */
    public boolean isAuthorized(Player player, List<String> names);

    /**
     * Verifies that a player is allowed to protect blocks. True if unused.
     * @param player The player to be checked
     * @param block The sign block being placed
     * @return
     */
    public boolean canProtect(Player player, Block block);
}
