package com.daemitus.deadbolt;

import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class DoorSchedule implements Runnable {

    private int taskID = -1;
    private final PriorityBlockingQueue<DoorTask> taskList = new PriorityBlockingQueue<DoorTask>();

    public boolean start(final Deadbolt plugin) {
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
        while (!taskList.isEmpty() && time > taskList.peek().time && taskList.peek().block != null) {
            close(taskList.poll());
        }
    }

    public void add(Set<Block> set, int delta, boolean isNatural, Location loc) {
        for (DoorTask task : taskList) {
            if (set.contains(task.block)) {
                taskList.remove(task);
                set.remove(task.block);
            }
        }
        Long time = System.currentTimeMillis();
        boolean playSound = true;
        for (Block block : set) {
            taskList.add(new DoorTask(block, time + delta * 1000, isNatural, playSound));
            playSound = false;
        }
    }

    private void close(DoorTask task) {
        task.block.setData((byte) (task.block.getData() ^ 0x4));
        if (task.playSound)
            if ((Config.timerDoorSounds) && (task.isNatural || Config.doorSounds))
                task.block.getLocation().getWorld().playEffect(task.block.getLocation(), Effect.DOOR_TOGGLE, 0);
    }

    private class DoorTask implements Comparable<DoorTask> {

        public final Long time;
        public final Block block;
        public final boolean isNatural;
        public final boolean playSound;

        public DoorTask(Block block, Long time, boolean isNatural, boolean playSound) {
            this.block = block;
            this.time = time;
            this.isNatural = isNatural;
            this.playSound = playSound;
        }

        public int compareTo(DoorTask task) {
            return this.time.compareTo(task.time);
        }
    }
}
