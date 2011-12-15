package com.daemitus.deadbolt.tasks;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.ContainerBlock;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Door;
import org.bukkit.material.TrapDoor;

public final class ProtectionRegenTask implements Runnable {

    private BlockState state;
    private ItemStack[] contents;

    public ProtectionRegenTask(Block block) {
        this.state = block.getState();

        if (state instanceof ContainerBlock) {
            ContainerBlock cb = (ContainerBlock) state;
            Inventory inv = cb.getInventory();
            contents = inv.getContents();
            inv.clear();
            block.setTypeId(0, false);
        } else {
            block.setTypeId(0, false);
        }
    }

    @Override
    public void run() {
        replaceProtections();
    }

    public void replaceProtections() {
        Block block = state.getBlock();
        if (state.getData() instanceof Door) {
            block.setTypeIdAndData(state.getTypeId(), state.getData().getData(), false);
            Door door = (Door) state.getData();
            door.setTopHalf(door.isTopHalf());

        } else if (state instanceof Sign) {
            block.setTypeIdAndData(state.getTypeId(), state.getData().getData(), false);
            Sign sign = (Sign) block.getState();
            int i = 0;
            for (String line : ((Sign) state).getLines())
                sign.setLine(i++, line);

        } else if (state.getData() instanceof TrapDoor) {
            block.setTypeIdAndData(state.getTypeId(), state.getData().getData(), false);

        } else if (state instanceof ContainerBlock) {
            block.setTypeIdAndData(state.getTypeId(), state.getData().getData(), false);
            ContainerBlock cb = (ContainerBlock) state;
            cb.getInventory().setContents(contents);

        } else {
            block.setTypeIdAndData(state.getTypeId(), state.getData().getData(), false);
        }
    }
}