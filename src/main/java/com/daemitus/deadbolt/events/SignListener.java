package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Conf;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.DeadboltGroup;
import com.daemitus.deadbolt.Perm;
import com.daemitus.deadbolt.bridge.Bridge;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.TrapDoor;

public class SignListener extends org.bukkit.event.block.BlockListener {

    private final Deadbolt plugin;

    public SignListener(final Deadbolt plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvent(Type.SIGN_CHANGE, this, Priority.Normal, plugin);
    }

    @Override
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String[] lines = event.getLines();
        if (player.hasPermission(Perm.user_color))
            for (int i = 0; i < 4; i++)
                lines[i] = Conf.replaceColor(event.getLine(i));

        boolean isPrivate = Conf.isPrivate(lines[0]);
        boolean isMoreUsers = Conf.isMoreUsers(lines[0]);
        if (isPrivate)
            for (int i = 0; i < 4; i++)
                lines[i] = Conf.default_color_private[i] + lines[i];
        else if (isMoreUsers)
            for (int i = 0; i < 4; i++)
                lines[i] = Conf.default_color_moreusers[i] + lines[i];
        else
            return;


        String status = "";
        if (block.getType().equals(Material.WALL_SIGN)) {
            status = checkWallSign(player, block, isPrivate);
        } else {
            block.setType(Material.WALL_SIGN);
            for (byte data = 2; !status.equals("valid") && data < 6; data++) {
                block.setData(data);
                String newStatus = checkWallSign(player, block, isPrivate);
                if (!newStatus.isEmpty())
                    status = newStatus;
            }
        }

        if (status.equals("valid") && Bridge.canProtect(player, block)) {
            if (isPrivate) {
                String owner = Conf.stripColor(lines[1]);
                if (owner.isEmpty())
                    lines[1] += player.getName();
                else if (player.hasPermission(Perm.admin_create) && plugin.getServer().getPlayer(owner) == null)
                    Conf.sendMessage(player, String.format(Conf.msg_admin_warning_player_not_found, owner), ChatColor.YELLOW);
                else if (!player.hasPermission(Perm.admin_create) && !owner.equalsIgnoreCase(player.getName()))
                    lines[1] += player.getName();
            }
            Conf.setLines((Sign) block.getState(), lines);
            return;
        } else {
            if (status.isEmpty())
                status = Conf.msg_deny_sign_private_nothing_nearby;
            Conf.sendMessage(player, status, ChatColor.RED);
        }

        event.setCancelled(true);
        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.SIGN, 1));
        block.setType(Material.AIR);
    }

    private String checkWallSign(Player player, Block signBlock, boolean isPrivate) {
        List<DeadboltGroup> dbgList = new ArrayList<DeadboltGroup>();

        //Similar to getRelated, however because the sign is not yet initialized,
        //the logic must be done here again.

        Block attached = DeadboltGroup.getBlockSignAttachedTo(signBlock);
        //get whatever the sign is directly attached to
        switch (attached.getType()) {
            case AIR:
                return "";
            case CHEST:
            case FURNACE:
            case BURNING_FURNACE:
            case DISPENSER:
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
            case TRAP_DOOR:
            case FENCE_GATE:
                dbgList.add(DeadboltGroup.getRelated(attached));
        }

        //check for doors above/below the attached
        for (BlockFace bf : Conf.VERTICAL_FACES) {
            Block adjacent = attached.getRelative(bf);
            switch (adjacent.getType()) {
                case WOODEN_DOOR:
                case IRON_DOOR_BLOCK:
                    dbgList.add(DeadboltGroup.getRelated(adjacent));
            }
        }

        //check for trapdoors above/below the sign itself, regardless of orientation
        for (BlockFace bf : Conf.VERTICAL_FACES) {
            Block adjacent = signBlock.getRelative(bf);
            switch (adjacent.getType()) {
                case TRAP_DOOR:
                    dbgList.add(DeadboltGroup.getRelated(adjacent));
            }
        }

        //assume attached is the hinge, look for trap doors nearby
        //assume a fencegate is adjacent to attached
        for (BlockFace bf : Conf.CARDINAL_FACES) {
            Block adjacent = attached.getRelative(bf);
            switch (adjacent.getType()) {
                case TRAP_DOOR:
                    TrapDoor trap = (TrapDoor) adjacent.getState().getData();
                    Block hinge = adjacent.getRelative(trap.getAttachedFace());
                    if (hinge.equals(attached))
                        dbgList.add(DeadboltGroup.getRelated(adjacent));
                    break;
                case FENCE_GATE:
                    dbgList.add(DeadboltGroup.getRelated(adjacent));
                    break;
            }
        }

        String owner = null;
        for (DeadboltGroup dbg : dbgList)
            if (owner == null && dbg.getOwner() != null && dbg.isOwner(player))
                owner = player.getName();
            else
                owner = dbg.getOwner();

        if (isPrivate) {
            //is it owned?            
            if (owner != null)
                return Conf.msg_deny_sign_private_already_owned;

            //check against the block it is immediately on
            switch (attached.getType()) {
                case CHEST:
                    return player.hasPermission(Perm.user_create_chest) ? "valid" : String.format(Conf.msg_deny_block_perm, attached.getType().name());
                case DISPENSER:
                    return player.hasPermission(Perm.user_create_dispenser) ? "valid" : String.format(Conf.msg_deny_block_perm, attached.getType().name());
                case FURNACE:
                    return player.hasPermission(Perm.user_create_furnace) ? "valid" : String.format(Conf.msg_deny_block_perm, attached.getType().name());
                case BURNING_FURNACE:
                    return player.hasPermission(Perm.user_create_furnace) ? "valid" : String.format(Conf.msg_deny_block_perm, attached.getType().name());
                case WOODEN_DOOR:
                    return player.hasPermission(Perm.user_create_door) ? "valid" : String.format(Conf.msg_deny_block_perm, attached.getType().name());
                case IRON_DOOR_BLOCK:
                    return player.hasPermission(Perm.user_create_door) ? "valid" : String.format(Conf.msg_deny_block_perm, attached.getType().name());
                case TRAP_DOOR:
                    return player.hasPermission(Perm.user_create_trapdoor) ? "valid" : String.format(Conf.msg_deny_block_perm, attached.getType().name());
                case FENCE_GATE:
                    return player.hasPermission(Perm.user_create_fencegate) ? "valid" : String.format(Conf.msg_deny_block_perm, attached.getType().name());
                default:
                    boolean valid = false;
                    //check for doors above/below the attached
                    for (BlockFace bf : Conf.VERTICAL_FACES) {
                        Block findDoor = attached.getRelative(bf);
                        switch (findDoor.getType()) {
                            case WOODEN_DOOR:
                            case IRON_DOOR_BLOCK:
                                if (!player.hasPermission(Perm.user_create_door))
                                    return String.format(Conf.msg_deny_block_perm, findDoor.getType().name());
                                else
                                    valid = true;
                        }
                    }

                    //check for trapdoors above/below the sign itself
                    for (BlockFace bf : Conf.VERTICAL_FACES) {
                        Block findTrap = signBlock.getRelative(bf);
                        switch (findTrap.getType()) {
                            case TRAP_DOOR:
                                if (!player.hasPermission(Perm.user_create_trapdoor))
                                    return String.format(Conf.msg_deny_block_perm, findTrap.getType().name());
                                else
                                    valid = true;
                        }
                    }

                    //assume against is the hinge, look for trap doors nearby
                    //assume a fencegate is adjacent
                    for (BlockFace bf : Conf.CARDINAL_FACES) {
                        Block adjacent = attached.getRelative(bf);
                        switch (adjacent.getType()) {
                            case TRAP_DOOR:
                                TrapDoor trapDoor = (TrapDoor) adjacent.getState().getData();
                                if (adjacent.getRelative(trapDoor.getAttachedFace()).equals(attached)) {
                                    if (!player.hasPermission(Perm.user_create_trapdoor))
                                        return String.format(Conf.msg_deny_block_perm, adjacent.getType().name());
                                    else
                                        valid = true;
                                }
                                break;
                            case FENCE_GATE:
                                if (!player.hasPermission(Perm.user_create_fencegate))
                                    String.format(Conf.msg_deny_block_perm, adjacent.getType().name());
                                else
                                    valid = true;
                        }
                    }
                    if (valid)
                        return "valid";
                    else
                        return "";
            }
        } else {
            if (owner == null) {
                return Conf.msg_deny_sign_moreusers_no_private;
            } else if (!owner.equalsIgnoreCase(player.getName())) {
                if (player.hasPermission(Perm.admin_create)) {
                    return "valid";
                } else {
                    return Conf.msg_deny_sign_moreusers_already_owned;
                }
            } else {
                return "valid";
            }
        }
    }
}
