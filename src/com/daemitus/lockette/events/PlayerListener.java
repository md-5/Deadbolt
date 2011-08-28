package com.daemitus.lockette.events;

import com.daemitus.lockette.Config;
import com.daemitus.lockette.Lockette;
import com.daemitus.lockette.Perm;
import com.daemitus.lockette.Util;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.ContainerBlock;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Result;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;
import org.bukkit.material.TrapDoor;
import org.bukkit.plugin.PluginManager;

public class PlayerListener extends org.bukkit.event.player.PlayerListener {

    private final Lockette plugin;
    private final String timerPattern = "\\[.{1,11}:[123456789]\\]";

    public PlayerListener(final Lockette plugin) {
        this.plugin = plugin;
    }

    public void registerEvents(final PluginManager pm) {
        pm.registerEvent(Type.PLAYER_INTERACT, this, Priority.Normal, plugin);
        pm.registerEvent(Type.PLAYER_QUIT, this, Priority.Normal, plugin);
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            BlockState state = block.getState();
            MaterialData data = state.getData();
            if (data instanceof Door || data instanceof TrapDoor) {
                if (!interactDoor(player, block)) {
                    event.setUseInteractedBlock(Result.DENY);
                    event.setUseItemInHand(Result.DENY);
                    plugin.sendMessage(player, Config.msg_deny_door_access, ChatColor.RED);
                }
            }
        } else if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            BlockState state = block.getState();
            MaterialData data = state.getData();
            if (data instanceof Door || data instanceof TrapDoor) {
                if (!interactDoor(player, block)) {
                    event.setUseInteractedBlock(Result.DENY);
                    event.setUseItemInHand(Result.DENY);
                    plugin.sendMessage(player, Config.msg_deny_door_access, ChatColor.RED);
                }
            } else if (state instanceof ContainerBlock) {
                if (!interactContainer(player, block)) {
                    event.setUseInteractedBlock(Result.DENY);
                    event.setUseItemInHand(Result.DENY);
                    plugin.sendMessage(player, Config.msg_deny_container_access, ChatColor.RED);
                }
            } else if (block.getType().equals(Material.WALL_SIGN)) {
                if (!interactSign(player, block)) {
                    event.setUseInteractedBlock(Result.DENY);
                    event.setUseItemInHand(Result.DENY);
                    plugin.sendMessage(player, Config.msg_deny_sign_selection, ChatColor.RED);
                }
            }
        }
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.clearSelectedSign(event.getPlayer());
    }

    public boolean interactDoor(Player player, Block block) {
        Block owner = Util.getOwnerSign(block);
        if (owner == null)
            return true;
        if (!Util.isAuthorized(player.getName(), block))
            if (Perm.override(player, Perm.admin_bypass)) {
                plugin.sendMessage(player, String.format(Config.msg_admin_bypass, ((Sign) owner.getState()).getLine(1)), ChatColor.RED);
            } else
                return false;
        Block ownerAttached = Util.getBlockSignAttachedTo(owner);
        int delay = getDelayFromSign((Sign) owner.getState());
        Set<Block> doorBlocks = new HashSet<Block>();
        doorBlocks = toggleDoor(block, ownerAttached, isNaturalOpen(block));
        if (Config.timerDoorsAlwaysOn)
            plugin.scheduleDoor(doorBlocks, delay == 0 ? Config.timerDoorsAlwaysOnDelay : delay);
        else if (delay > 0) {
            plugin.scheduleDoor(doorBlocks, delay);
        }
        return true;
    }

    private Set<Block> toggleDoor(Block block, Block keyBlock, boolean naturalOpen) {
        Set<Block> set = new HashSet<Block>();
        set.add(block);
        if (!naturalOpen)
            toggleSingleBlock(block);

        for (BlockFace bf : Util.verticalBlockFaces) {
            Block verticalBlock = block.getRelative(bf);
            if (verticalBlock.getType().equals(block.getType())) {
                set.add(verticalBlock);
                if (!naturalOpen)
                    toggleSingleBlock(verticalBlock);
            }
        }

        if (keyBlock != null) {
            for (BlockFace bf : Util.horizontalBlockFaces) {
                Block adjacent = block.getRelative(bf);
                if (adjacent.getType().equals(block.getType())
                    && ((adjacent.getX() == keyBlock.getX() && adjacent.getZ() == keyBlock.getZ())
                        || (block.getX() == keyBlock.getX() && block.getZ() == keyBlock.getZ())))
                    set.addAll(toggleDoor(adjacent, null, false));
            }
        }
        return set;
    }

    private Block toggleSingleBlock(Block block) {
        block.setData((byte) (block.getData() ^ 0x4));
        return block;
    }

    private boolean isNaturalOpen(Block block) {
        switch (block.getType()) {
            case WOODEN_DOOR:
                return true;
            case TRAP_DOOR:
                return true;
            case IRON_DOOR_BLOCK:
                return false;
            default:
                return true;
        }
    }

    private int getDelayFromSign(Sign sign) {
        for (int i = 2; i < 4; i++) {
            String text = Util.stripColor(sign.getLine(i));
            if (!text.matches(timerPattern))
                continue;

            String word = text.substring(1, text.length() - 3);
            if (!word.equalsIgnoreCase(Config.signtext_timer) && !word.equalsIgnoreCase(Config.signtext_timer_locale))
                continue;

            return Integer.valueOf(text.substring(text.length() - 2, text.length() - 1));
        }
        return 0;
    }

    public boolean interactContainer(Player player, Block block) {
        String owner = Util.getOwnerName(block);
        if (owner.equals(""))
            return true;
        if (!Util.isAuthorized(player.getName(), block))
            if (Perm.override(player, Perm.admin_snoop))
                plugin.sendBroadcast(Perm.admin_broadcast_snoop,
                                     String.format(Config.msg_admin_snoop, player.getName(), owner),
                                     ChatColor.RED);
            else
                return false;
        return true;
    }

    public boolean interactSign(Player player, Block block) {
        String owner = Util.getOwnerName(block);
        if (owner.equals(""))
            return false;
        if (!owner.equalsIgnoreCase(player.getName()))
            if (Config.adminSign && Perm.override(player, Perm.admin_signs))
                plugin.sendMessage(player, String.format(Config.msg_admin_signs, owner), ChatColor.RED);
            else
                return false;
        plugin.sendMessage(player, Config.cmd_sign_selected, ChatColor.GOLD);
        plugin.setSelectedSign(player, block);
        return true;
    }
}
