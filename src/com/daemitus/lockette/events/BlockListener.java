package com.daemitus.lockette.events;

import com.daemitus.lockette.Lockette;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;
import org.bukkit.material.TrapDoor;

public class BlockListener extends org.bukkit.event.block.BlockListener {

    private final Lockette plugin;

    public BlockListener(final Lockette plugin) {
        this.plugin = plugin;
    }

    public void registerEvents() {
        plugin.pm.registerEvent(Type.BLOCK_BREAK, this, Priority.Normal, plugin);
        plugin.pm.registerEvent(Type.BLOCK_DAMAGE, this, Priority.Normal, plugin);
        plugin.pm.registerEvent(Type.BLOCK_PLACE, this, Priority.Normal, plugin);
        plugin.pm.registerEvent(Type.REDSTONE_CHANGE, this, Priority.Normal, plugin);
        plugin.pm.registerEvent(Type.SIGN_CHANGE, this, Priority.Normal, plugin);
        plugin.pm.registerEvent(Type.BLOCK_PISTON_EXTEND, this, Priority.Normal, plugin);
        plugin.pm.registerEvent(Type.BLOCK_PISTON_RETRACT, this, Priority.Normal, plugin);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled())
            return;
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String owner = plugin.logic.getOwnerName(block);
        if (owner.equals("") || owner.equalsIgnoreCase(plugin.logic.truncate(player.getName())))
            return;
        event.setCancelled(true);
        plugin.logic.sendErrorMessage(player, plugin.cm.msg_user_denied_break_locale);

    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled())
            return;
        Block against = event.getBlockAgainst();
        if (against.getType().equals(Material.WALL_SIGN) && plugin.logic.isProtected(against)) {
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
                plugin.logic.sendErrorMessage(player, plugin.cm.msg_user_denied_chest_place_locale);
            }
        } else if (data instanceof Door) {
            if (!onDoorPlace(player, block)) {
                event.setCancelled(true);
                block.setType(Material.SAND);
                block.setType(Material.AIR);
                block.getRelative(BlockFace.UP).setType(Material.SAND);
                block.getRelative(BlockFace.UP).setType(Material.AIR);
                plugin.logic.sendErrorMessage(player, plugin.cm.msg_user_denied_door_place_locale);
            }
        } else if (data instanceof TrapDoor) {
            if (!onTrapDoorPlace(player, against)) {
                event.setCancelled(true);
                plugin.logic.sendErrorMessage(player, plugin.cm.msg_user_denied_trapdoor_place_locale);
            }
        }
    }

    private boolean onChestPlace(Player player, Block block) {
        for (BlockFace bf : plugin.logic.horizontalBlockFaces) {
            Block adjacent = block.getRelative(bf);
            if (!adjacent.getType().equals(block.getType()))
                continue;
            String owner = plugin.logic.getOwnerName(adjacent);
            if (owner.equals("") || owner.equals(plugin.logic.truncate(player.getName())))
                continue;
            return false;
        }
        return true;
    }

    private boolean onDoorPlace(Player player, Block block) {
        for (BlockFace bf : plugin.logic.horizontalBlockFaces) {
            Block adjacent = block.getRelative(bf);
            if (!adjacent.getType().equals(block.getType()))
                continue;
            String owner = plugin.logic.getOwnerName(adjacent);
            if (owner.equals("") || owner.equals(plugin.logic.truncate(player.getName())))
                continue;
            return false;
        }
        String owner = plugin.logic.getOwnerName(block.getRelative(0, 2, 0));
        if (owner.equals("") || owner.equals(plugin.logic.truncate(player.getName())))
            return true;
        return false;
    }

    private boolean onTrapDoorPlace(Player player, Block against) {
        String owner = plugin.logic.getOwnerName(against);
        if (owner.equals("") || owner.equals(plugin.logic.truncate(player.getName()))) {
            return true;
        }
        return false;
    }

    @Override
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String text = plugin.logic.stripColor(event.getLine(0));

        boolean primary = false;
        boolean moreusers = false;
        if (text.equalsIgnoreCase(plugin.cm.ingame_sign_primary)
            || text.equalsIgnoreCase(plugin.cm.ingame_sign_primary_locale))
            primary = true;
        else if (text.equalsIgnoreCase(plugin.cm.ingame_sign_moreusers)
                 || text.equalsIgnoreCase(plugin.cm.ingame_sign_moreusers_locale))
            moreusers = true;

        if (!primary && !moreusers)
            return;

        //TODO NEEDS ALOT OF WORK
        if (block.getType().equals(Material.WALL_SIGN)) {//TODO DOOR WORK
            String owner = plugin.logic.getOwnerName(plugin.logic.getBlockSignAttachedTo(block));
            if (primary && !owner.equals("")) {
                plugin.logic.sendErrorMessage(player, plugin.cm.msg_user_denied_sign_place_primary_locale);
                event.setCancelled(true);
                return;
            } else if (moreusers && !owner.equalsIgnoreCase(plugin.logic.truncate(player.getName()))) {
                plugin.logic.sendErrorMessage(player, plugin.cm.msg_user_denied_sign);
                event.setCancelled(true);
                return;
            }
        } else {
            String lines[] = new String[4];
            for (int i = 0; i < 4; i++) {
                lines[i] = event.getLine(i);
            }
            boolean valid = false;
            block.setType(Material.WALL_SIGN);
            for (int i = 2; i < 6; i++) {
                block.setData((byte) i);
                Block against = plugin.logic.getBlockSignAttachedTo(block);
                String owner = plugin.logic.getOwnerName(against);
                if (primary && owner.equals("")) {
                    for (BlockFace bf : plugin.logic.horizontalBlockFaces) {
                        Block adjacent = block.getRelative(bf);
                        if (adjacent.getType().equals(Material.CHEST) || adjacent.getState().getData() instanceof Door ) {
                            valid = true;
                            break;
                        }
                    }
                } else if (moreusers && owner.equalsIgnoreCase(plugin.logic.truncate(player.getName()))) {
                    valid = true;
                    break;
                }
            }

            if (valid) {
                BlockState state = block.getState();
                Sign sign = (Sign) state;
                for (int i = 0; i < 4; i++) {
                    sign.setLine(i, lines[i]);
                }
                sign.update(true);
            } else {
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.SIGN, 1));
                block.setType(Material.AIR);
            }
        }

        /*
        if (!typeWallSign) {

        block.setData(face);

        Sign sign = (Sign) block.getState();

        sign.setLine(0, event.getLine(0));
        sign.setLine(1, event.getLine(1));
        sign.setLine(2, event.getLine(2));
        sign.setLine(3, event.getLine(3));
        sign.update(true);
        } else {
        block.setData(face);
        }

        if (anyone) {
        Lockette.log.info(plugin.getDescription().getName() + ": (Admin) " + player.getName() + " has claimed a container for " + event.getLine(1) + ".");

        if (!Lockette.msgAdmin)
        return;
        String msgString;
        if (!plugin.playerOnline(event.getLine(1)))
        msgString = Lockette.strings.getString("msg-admin-claim-error");
        else
        msgString = Lockette.strings.getString("msg-admin-claim");
        if ((msgString == null) || (msgString.isEmpty()))
        return;
        String msgString = msgString.replaceAll("\\*\\*\\*", event.getLine(1));
        player.sendMessage(ChatColor.RED + "Lockette: " + msgString);
        } else {
        Lockette.log.info(plugin.getDescription().getName() + ": " + player.getName() + " has claimed a container.");

        if (!Lockette.msgOwner)
        return;
        String msgString = Lockette.strings.getString("msg-owner-claim");
        if ((msgString == null) || (msgString.isEmpty()))
        return;
        player.sendMessage(ChatColor.GOLD + "Lockette: " + msgString);
        }

        }
        else

        if ((text.equals(



        "[more users]")) || (text.equals(Lockette.altMoreUsers))) {
        Player player = event.getPlayer();

        Block[] checkBlock = new Block[4];
        Block signBlock = null;
        Sign sign = null;
        byte face = 0;

        int length = player.getName().length();

        if (length > 15)
        length = 15;

        if ((Lockette.protectDoors) && (typeWallSign)) {
        checkBlock[0] = Lockette.getSignAttachedBlock(block);

        if ((checkBlock[0] != null)
        && (!isInList(checkBlock[0].getTypeId(), this.materialListBad))) {
        signBlock = Lockette.findBlockOwner(checkBlock[0]);

        if (signBlock != null) {
        sign = (Sign) signBlock.getState();

        if (sign.getLine(1).replaceAll("(?i)§[0-F]", "").equals(player.getName().substring(0, length))) {
        face = block.getData();
        }

        }

        }

        }

        if (face == 0) {
        checkBlock[0] = block.getRelative(BlockFace.NORTH);
        checkBlock[1] = block.getRelative(BlockFace.EAST);
        checkBlock[2] = block.getRelative(BlockFace.SOUTH);
        checkBlock[3] = block.getRelative(BlockFace.WEST);

        for (int x = 0; x < 4; x++) {
        if ((!isInList(checkBlock[x].getTypeId(), this.materialList)) && ((!Lockette.protectDoors)
        || (!isInList(checkBlock[x].getTypeId(), this.materialListDoors)))) {
        continue;
        }
        signBlock = Lockette.findBlockOwner(checkBlock[x]);

        if (signBlock != null) {
        sign = (Sign) signBlock.getState();

        if (sign.getLine(1).replaceAll("(?i)§[0-F]", "").equals(player.getName().substring(0, length))) {
        face = this.faceList[x];

        break;
        }

        }

        }

        }

        if (face == 0) {
        event.setLine(0, "[?]");
        if (sign != null) {
        if (!Lockette.msgError)
        return;
        String msgString = Lockette.strings.getString("msg-error-adduser-owned");
        if ((msgString == null) || (msgString.isEmpty()))
        return;
        msgString = msgString.replaceAll("\\*\\*\\*", sign.getLine(1));
        player.sendMessage(ChatColor.RED + "Lockette: " + msgString);
        } else {
        if (!Lockette.msgError)
        return;
        String msgString = Lockette.strings.getString("msg-error-adduser");
        if ((msgString == null) || (msgString.isEmpty()))
        return;
        player.sendMessage(ChatColor.RED + "Lockette: " + msgString);
        }

        return;
        }

        event.setCancelled(false);
        if (!typeWallSign) {
        block.setType(Material.WALL_SIGN);
        block.setData(face);

        sign = (Sign) block.getState();

        sign.setLine(0, event.getLine(0));
        sign.setLine(1, event.getLine(1));
        sign.setLine(2, event.getLine(2));
        sign.setLine(3, event.getLine(3));
        sign.update(true);
        } else {
        block.setData(face);
        }

        if (!Lockette.msgOwner)
        return;
        String msgString = Lockette.strings.getString("msg-owner-adduser");
        if ((msgString == null) || (msgString.isEmpty()))
        return;
        player.sendMessage(ChatColor.GOLD + "Lockette: " + msgString);
        }

        @Override
        public void onBlockRedstoneChange(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        Material type = block.getType();
        BlockState state = block.getState();
        MaterialData data = state.getData();
        if (!(data instanceof Door || data instanceof TrapDoor))
        return;

        List<String> names = plugin.logic.getAllNames(block);
        if (names.isEmpty()
        || names.contains(plugin.cm.ingame_sign_everyone)
        || names.contains(plugin.cm.ingame_sign_everyone_locale))
        return;
        event.setNewCurrent(event.getOldCurrent());
        }

        @Override
        public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        }

        @Override
        public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        }
        }
         */    }
}
