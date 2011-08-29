package com.daemitus.lockette;

import java.util.PriorityQueue;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
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

    public void add(Set<Block> set, int delta) {
        Long time = System.currentTimeMillis();
        for (Block block : set) {
            taskList.add(new DoorTask(block, time + delta * 1000));
        }
    }

    private void close(DoorTask task) {
        Block block = task.block;
        block.setData((byte) (block.getData() ^ 0x4));
        if (Config.doorSounds)
            block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
    }

    private class DoorTask implements Comparable<DoorTask> {

        Block block;
        Long time;

        public DoorTask(Block block, Long time) {
            this.block = block;
            this.time = time;
        }

        public int compareTo(DoorTask task) {
            return this.time.compareTo(task.time);
        }
    }
}
