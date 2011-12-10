package com.daemitus.deadbolt.tasks;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.block.Block;

public final class ToggleDoorTask implements Runnable {

    public static Map<Block, Integer> timedBlocks = new HashMap<Block, Integer>();
    private final Block block;

    public ToggleDoorTask(Block block) {
        this.block = block;
    }

    @Override
    public void run() {
        block.setData((byte) (block.getData() ^ 0x4));
    }
}
