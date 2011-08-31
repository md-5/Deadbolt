package com.daemitus.lockette;

import java.util.PriorityQueue;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class DoorSchedule implements Runnable {

    private int taskID = -1;
    private final PriorityQueue<DoorTask> taskList = new PriorityQueue<DoorTask>();

    public boolean start(final Lockette plugin) {
        if (taskID != -1)
            return false;
        taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 100L, 10L);
        return taskID != -1;
    }

    public boolean stop() {
        if (taskID == -1)
            return false;
        Bukkit.getServer().getScheduler().cancelTask(taskID);
        taskID = -1;

        while (!taskList.isEmpty()) {
            DoorTask task = taskList.poll();
            close(task);
        }
        return true;
    }

    public void run() {
        if (taskList.isEmpty())
            return;
        Long time = System.currentTimeMillis();
        while (!taskList.isEmpty() && time > taskList.peek().time) {
            close(taskList.poll());
        }
    }

    public void add(Set<Block> set, int delta, boolean isNatural, Location loc) {
        taskList.add(new DoorTask(set, System.currentTimeMillis() + delta * 1000, isNatural, loc));
    }

    private void close(DoorTask task) {
        for (Block block : task.set)
            block.setData((byte) (block.getData() ^ 0x4));
        if ((Config.timerDoorSounds) && (task.isNatural || Config.doorSounds))
            task.loc.getWorld().playEffect(task.loc, Effect.DOOR_TOGGLE, 0);
    }

    private class DoorTask implements Comparable<DoorTask> {

        public final Long time;
        public final Set<Block> set;
        public final boolean isNatural;
        public final Location loc;

        public DoorTask(Set<Block> set, Long time, boolean isNatural, Location loc) {
            this.set = set;
            this.time = time;
            this.isNatural = isNatural;
            this.loc = loc;

        }

        public int compareTo(DoorTask task) {
            return this.time.compareTo(task.time);
        }
    }
}
