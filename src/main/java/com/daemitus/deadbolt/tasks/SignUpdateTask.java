package com.daemitus.deadbolt.tasks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class SignUpdateTask implements Runnable {

    private final Block block;

    public SignUpdateTask(Block block) {
        this.block = block;
    }

    public void run() {
        if (block.getType() == Material.WALL_SIGN) {
            ((Sign) block.getState()).update(true);
        }
    }
}
