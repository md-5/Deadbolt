package com.daemitus.deadbolt;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.bukkit.Effect;
import org.bukkit.block.Block;

public class ToggleDoorTask implements Runnable {

    public static Set<Block> timedBlocks = new HashSet<Block>();
    private final Block block;
    private final boolean sound;

    public ToggleDoorTask(Block block, boolean sound) {
        this.block = block;
        this.sound = sound;
    }

    public void run() {
        if (timedBlocks.remove(block)) {
            block.setData((byte) (block.getData() ^ 0x4));
            if (sound) {
                block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 10);
            }
        }
    }

    public static void cleanup() {
        Iterator<Block> iter = timedBlocks.iterator();
        while (iter.hasNext()) {
            Block next = iter.next();
            next.setData((byte) (next.getData() ^ 0x4));
            iter.remove();
        }
    }
}
