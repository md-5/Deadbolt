package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Config;
import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Perm;
import com.daemitus.deadbolt.Util;
import com.daemitus.deadbolt.bridge.Bridge;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;
import org.bukkit.material.TrapDoor;
import org.bukkit.plugin.PluginManager;

public class BlockListener extends org.bukkit.event.block.BlockListener {

    private final Set<Player> reminder = new HashSet<>();
    private final Deadbolt plugin;

    public BlockListener(final Deadbolt plugin) {
        this.plugin = plugin;
    }

    public void registerEvents(final PluginManager pm) {
        pm.registerEvent(Type.BLOCK_BREAK, this, Priority.Low, plugin);
        pm.registerEvent(Type.BLOCK_PLACE, this, Priority.Normal, plugin);
        pm.registerEvent(Type.REDSTONE_CHANGE, this, Priority.Low, plugin);
        pm.registerEvent(Type.SIGN_CHANGE, this, Priority.Normal, plugin);
        pm.registerEvent(Type.BLOCK_PISTON_EXTEND, this, Priority.Low, plugin);
        pm.registerEvent(Type.BLOCK_PISTON_RETRACT, this, Priority.Low, plugin);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String owner = Util.getOwnerName(block);
        if (owner.equals("") || owner.equalsIgnoreCase(Util.truncate(player.getName())))
            return;
        if (player.hasPermission(Perm.admin_break)) {
            Util.sendBroadcast(Perm.admin_broadcast_break,
                    String.format(Config.msg_admin_break, player.getName(), owner),
                    ChatColor.RED);
            Deadbolt.logger.log(Level.INFO, String.format("Deadbolt: " + Config.msg_admin_break, player.getName(), owner));
            return;
        }
        event.setCancelled(true);
        if (block.getType().equals(Material.WALL_SIGN))
            ((Sign) block.getState()).update(true);
        Util.sendMessage(player, Config.msg_deny_block_break, ChatColor.RED);
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;
        Block against = event.getBlockAgainst();
        if (against.getType().equals(Material.WALL_SIGN) && Util.isProtected(against)) {
            event.setCancelled(true);
            return;
        }
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material type = block.getType();
        BlockState state = block.getState();
        MaterialData data = state.getData();

        if (type.equals(Material.CHEST)) {
            if (!onChestPlace(player, block)) {
                event.setCancelled(true);
                Util.sendMessage(player, Config.msg_deny_chest_expansion, ChatColor.RED);
            } else {
                if (!reminder.contains(player)) {
                    reminder.add(player);
                    Util.sendMessage(player, Config.msg_reminder_lock_your_chests, ChatColor.GOLD);
                }
            }
        } else if (data instanceof Door) {
            if (!onDoorPlace(player, block)) {
                event.setCancelled(true);
                block.setType(Material.SAND);
                block.setType(Material.AIR);
                block.getRelative(BlockFace.UP).setType(Material.SAND);
                block.getRelative(BlockFace.UP).setType(Material.AIR);
                Util.sendMessage(player, Config.msg_deny_door_expansion, ChatColor.RED);
            }
        } else if (data instanceof TrapDoor) {
            if (!onTrapDoorPlace(player, against)) {
                event.setCancelled(true);
                Util.sendMessage(player, Config.msg_deny_trapdoor_placement, ChatColor.RED);
            }
        }
    }

    private boolean onChestPlace(Player player, Block block) {
        for (BlockFace bf : Util.horizontalBlockFaces) {
            Block adjacent = block.getRelative(bf);
            if (!adjacent.getType().equals(block.getType()))
                continue;
            String owner = Util.getOwnerName(adjacent);
            if (owner.equals("") || owner.equals(Util.truncate(player.getName())))
                continue;
            return false;
        }
        return true;
    }

    private boolean onDoorPlace(Player player, Block block) {
        for (BlockFace bf : Util.horizontalBlockFaces) {
            Block adjacent = block.getRelative(bf);
            if (!adjacent.getType().equals(block.getType()))
                continue;
            String owner = Util.getOwnerName(adjacent);
            if (owner.equals("") || owner.equals(Util.truncate(player.getName())))
                continue;
            return false;
        }
        String owner = Util.getOwnerName(block.getRelative(0, 2, 0));
        if (owner.equals("") || owner.equals(Util.truncate(player.getName())))
            return true;
        return false;
    }

    private boolean onTrapDoorPlace(Player player, Block against) {
        String owner = Util.getOwnerName(against);
        if (owner.equals("") || owner.equals(Util.truncate(player.getName()))) {
            return true;
        }
        return false;
    }

    @Override
    public void onSignChange(SignChangeEvent event) {
        if (event.getPlayer().hasPermission(Perm.user_color))
            for (int i = 0; i < 4; i++)
                event.setLine(i, event.getLine(i).replaceAll(Util.patternFindColor, Util.patternReplaceColor));

        String identifier = Util.stripColor(event.getLine(0));
        boolean isPrivate =
                identifier.equalsIgnoreCase(Config.signtext_private)
                || identifier.equalsIgnoreCase(Config.signtext_private_locale);
        boolean isMoreUsers =
                identifier.equalsIgnoreCase(Config.signtext_moreusers)
                || identifier.equalsIgnoreCase(Config.signtext_moreusers_locale);
        if (!isPrivate && !isMoreUsers)
            return;

        
        String status = "";
        Player player = event.getPlayer();
        Block block = event.getBlock();
        boolean isWallSign = block.getType().equals(Material.WALL_SIGN);

        if (isWallSign) {
            status = checkWallSign(player, block, isPrivate);
        } else {
            block.setType(Material.WALL_SIGN);
            for (byte data = 2; !status.equals("valid") && data < 6; data++) {
                block.setData(data);
                status = checkWallSign(player, block, isPrivate);
            }
        }

        String lines[] = event.getLines();
        if (status.equals("valid")) {
            if (Bridge.canProtect(player, block)) {
                if (isPrivate) {
                    boolean isAdminAuth = player.hasPermission(Perm.admin_create);
                    if (!isAdminAuth) {
                        lines[1] = Util.truncate(player.getName());
                    } else {
                        if (lines[1].equals("")) {
                            lines[1] = Util.truncate(player.getName());
                        } else if (plugin.getServer().getPlayer(Util.stripColor(lines[1])) == null) {
                            Util.sendMessage(player, String.format(Config.msg_warning_player_not_found, lines[1]), ChatColor.YELLOW);
                        }
                    }
                }
                Sign sign = (Sign) block.getState();
                for (int i = 0; i < 4; i++)
                    sign.setLine(i, lines[i]);
                sign.update(true);
                return;
            }
        } else {
            if (status.equals(""))
                status = Config.msg_deny_sign_private_nothing_nearby;
            Util.sendMessage(player, status, ChatColor.RED);
        }
        event.setCancelled(true);
        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.SIGN, 1));
        block.setType(Material.AIR);
    }

    private String checkWallSign(Player player, Block signBlock, boolean isPrivate) {
        Block against = Util.getBlockSignAttachedTo(signBlock);
        if (against == null)
            return "";
        String owner = Util.getOwnerName(against);

        if (isPrivate) {
            //is it owned?
            if (!owner.equals("")) {
                return Config.msg_deny_sign_private_already_owned;
            } else {
                //check against the block it is immediately on
                switch (against.getType()) {
                    case CHEST:
                        return player.hasPermission(Perm.user_create_chest) ? "valid" : Config.msg_deny_chest_perm;
                    case DISPENSER:
                        return player.hasPermission(Perm.user_create_dispenser) ? "valid" : Config.msg_deny_dispenser_perm;
                    case FURNACE:
                        return player.hasPermission(Perm.user_create_furnace) ? "valid" : Config.msg_deny_furnace_perm;
                    case BURNING_FURNACE:
                        return player.hasPermission(Perm.user_create_furnace) ? "valid" : Config.msg_deny_furnace_perm;
                    case WOODEN_DOOR:
                        return player.hasPermission(Perm.user_create_door) ? "valid" : Config.msg_deny_door_perm;
                    case IRON_DOOR_BLOCK:
                        return player.hasPermission(Perm.user_create_door) ? "valid" : Config.msg_deny_door_perm;
                    case TRAP_DOOR:
                        return player.hasPermission(Perm.user_create_trapdoor) ? "valid" : Config.msg_deny_trapdoor_perm;
                }

                //check for doors above/below
                for (BlockFace bf : Util.verticalBlockFaces) {
                    Block vertical = against.getRelative(bf);
                    if (vertical.getState().getData() instanceof Door)
                        return player.hasPermission(Perm.user_create_door) ? "valid" : Config.msg_deny_door_perm;
                }

                //look for trap doors nearby
                for (BlockFace bf : Util.horizontalBlockFaces) {
                    Block adjacent = against.getRelative(bf);
                    BlockState state = adjacent.getState();
                    MaterialData data = state.getData();
                    if (data instanceof TrapDoor) {
                        TrapDoor trapDoor = (TrapDoor) data;
                        if (adjacent.getRelative(trapDoor.getAttachedFace()).equals(against)) {
                            return player.hasPermission(Perm.user_create_trapdoor) ? "valid" : Config.msg_deny_trapdoor_perm;
                        }
                    }
                }
            }
        } else {
            if (owner.equals("")) {
                return Config.msg_deny_sign_moreusers_no_private;
            } else if (!owner.equalsIgnoreCase(Util.truncate(player.getName()))) {
                if (player.hasPermission(Perm.admin_create)) {
                    return "valid";
                } else {
                    return Config.msg_deny_sign_moreusers_already_owned;
                }
            } else {
                return "valid";
            }
        }
        //nothing found, let it iterate
        return "";
    }

    @Override
    public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        BlockState state = block.getState();
        MaterialData data = state.getData();
        if (!(data instanceof Door || data instanceof TrapDoor))
            return;
        if (!Config.redstoneProtection)
            return;
        List<String> names = Util.getAllNames(block);
        if (names.isEmpty()
                || names.contains(Config.signtext_everyone)
                || names.contains(Config.signtext_everyone_locale))
            return;
        event.setNewCurrent(event.getOldCurrent());
    }

    @Override
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (event.isCancelled())
            return;
        if (!Config.pistonProtection)
            return;

        Block piston = event.getBlock();
        BlockFace facing = event.getDirection();
        for (int i = 1; i <= event.getLength() + 1; i++) {
            Block check = piston.getRelative(facing.getModX() * i, facing.getModY() * i, facing.getModZ() * i);
            if (Util.isProtected(check)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Override
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (event.isCancelled())
            return;
        if (!Config.pistonProtection)
            return;
        Block block = event.getBlock();
        if (!block.getType().equals(Material.PISTON_STICKY_BASE))
            return;
        BlockFace facing = event.getDirection();
        Block extension = block.getRelative(facing);
        Block check = extension.getRelative(facing);
        if (Util.isProtected(check)) {
            event.setCancelled(true);
            extension.setType(Material.AIR);
            block.setData((byte) (block.getData() ^ 0x8));
        }
    }
}
