package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

public class SignListener implements Listener {

    private final DeadboltPlugin plugin = Deadbolt.getPlugin();

    public SignListener() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private enum Result {

        DENY_SIGN_PRIVATE_ALREADY_OWNED, ADMIN_SIGN_PLACED, DENY_SIGN_MOREUSERS_ALREADY_OWNED, DENY_SIGN_PRIVATE_NOTHING_NEARBY, DENY_SIGN_MOREUSERS_NO_PRIVATE, SUCCESS, PLACEHOLDER, DENY_BLOCK_PERM;
    }

    @EventHandler(ignoreCancelled = true)
    @SuppressWarnings("fallthrough")
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String[] lines = event.getLines();

        //fix for client-side sign edit hack
        if (event.getBlock().getType().equals(Material.WALL_SIGN)) {
            Sign sign = (Sign) event.getBlock().getState();
            String ident = Util.getLine(sign, 0);
            if (Deadbolt.getLanguage().isPrivate(ident) || Deadbolt.getLanguage().isMoreUsers(ident)) {
                event.setCancelled(true);
                return;
            }
        }
        //fix end

        String ident = Util.removeColor(lines[0]);
        boolean isPrivate = Deadbolt.getLanguage().isPrivate(ident);
        boolean isMoreUsers = Deadbolt.getLanguage().isMoreUsers(ident);
        if (!isPrivate && !isMoreUsers) {
            return;
        }

        Deadbolted db = null;
        Result result = Result.PLACEHOLDER;
        if (block.getType().equals(Material.WALL_SIGN)) {
            Sign sign = (Sign) block.getState();
            sign.setLine(0, isPrivate ? Deadbolt.getLanguage().signtext_private : Deadbolt.getLanguage().signtext_moreusers);
            sign.update();
            db = Deadbolt.get(block);
            result = validateSignPlacement(db, player, isPrivate);
        } else {
            for (byte b = 0x2; b < 0x6 && !result.equals(Result.SUCCESS) && !result.equals(Result.ADMIN_SIGN_PLACED); b++) {
                block.setTypeIdAndData(Material.WALL_SIGN.getId(), b, false);
                Sign sign = (Sign) block.getState();
                sign.setLine(0, isPrivate ? Deadbolt.getLanguage().signtext_private : Deadbolt.getLanguage().signtext_moreusers);
                sign.update();
                db = Deadbolt.get(block);
                Result newresult = validateSignPlacement(db, player, isPrivate);
                if (!newresult.equals(Result.DENY_SIGN_PRIVATE_NOTHING_NEARBY)) {
                    result = newresult;
                }
            }
        }

        switch (result) {
            case ADMIN_SIGN_PLACED:
                Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().msg_admin_sign_placed, db.getOwner());
            case SUCCESS:
                if (isPrivate) {
                    String owner = Util.formatForSign(lines[1]);
                    if (owner.isEmpty()) {
                        lines[1] = Util.formatForSign(player.getName());
                    } else if (player.hasPermission(Perm.admin_create)) {
                        if (plugin.getServer().getPlayerExact(owner) == null) {
                            Deadbolt.getConfig().sendMessage(player, ChatColor.YELLOW, Deadbolt.getLanguage().msg_admin_warning_player_not_found, owner);
                        }
                    } else {
                        lines[1] = Util.formatForSign(player.getName());
                    }
                }

                Sign sign = (Sign) block.getState();
                for (int i = 0; i < 4; i++) {
                    sign.setLine(i, Util.formatForSign(lines[i]));
                }
                sign.update();
                return;
            case DENY_SIGN_PRIVATE_ALREADY_OWNED:
                Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().msg_deny_sign_private_already_owned);
                break;
            case DENY_SIGN_MOREUSERS_ALREADY_OWNED:
                Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().msg_deny_sign_moreusers_already_owned);
                break;
            case DENY_SIGN_MOREUSERS_NO_PRIVATE:
                Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().msg_deny_sign_moreusers_no_private);
                break;
            case DENY_BLOCK_PERM:
                Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().msg_deny_block_perm);
                break;
            default:
                //case DENY_SIGN_PRIVATE_NOTHING_NEARBY:
                Deadbolt.getConfig().sendMessage(player, ChatColor.RED, Deadbolt.getLanguage().msg_deny_sign_private_nothing_nearby);
                break;
        }
        event.setCancelled(true);
        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.SIGN, 1));
        block.setType(Material.AIR);
    }

    private Result validateSignPlacement(Deadbolted db, Player player, boolean isPrivate) {
        if (db.isProtected()) {
            if (db.isOwner(player)) {
                //Is the owner
                if (isPrivate) {
                    //Already has [private], pop the sign
                    return Result.DENY_SIGN_PRIVATE_ALREADY_OWNED;
                } else {
                    //good
                    return Result.SUCCESS;
                }
            } else {
                if (isPrivate) {
                    //Already has [private], pop the sign
                    return Result.DENY_SIGN_PRIVATE_ALREADY_OWNED;
                } else {
                    if (player.hasPermission(Perm.admin_create)) {
                        //good, overridden
                        return Result.ADMIN_SIGN_PLACED;
                    } else {
                        //not authorized, pop sign
                        return Result.DENY_SIGN_MOREUSERS_ALREADY_OWNED;
                    }
                }
            }
        } else {
            if (isPrivate) {
                //Check if user can protect that type
                boolean chest = false;
                boolean dispenser = false;
                boolean furnace = false;
                boolean door = false;
                boolean trap = false;
                boolean gate = false;
                boolean brewery = false;
                boolean cauldron = false;
                boolean enchant = false;
                boolean beacon = false;
                boolean ender = false;
                boolean anvil = false;
                boolean hopper = false;
                boolean dropper = false;
                boolean trappedChest = false;
                for (Block setBlock : db.getBlocks()) {
                    //not authorized to protect?
                    switch (setBlock.getType()) {
                        case CHEST:
                            if (!chest && !(chest = player.hasPermission(Perm.user_create_chest))) {
                                return Result.DENY_BLOCK_PERM;
                            }
                            break;
                        case DISPENSER:
                            if (!dispenser && !(dispenser = player.hasPermission(Perm.user_create_dispenser))) {
                                return Result.DENY_BLOCK_PERM;
                            }
                            break;
                        case FURNACE:
                        case BURNING_FURNACE:
                            if (!furnace && !(furnace = player.hasPermission(Perm.user_create_furnace))) {
                                return Result.DENY_BLOCK_PERM;
                            }
                            break;
                        case WOODEN_DOOR:
                        case IRON_DOOR_BLOCK:
                        case SPRUCE_DOOR:
                        case BIRCH_DOOR:
                        case JUNGLE_DOOR:
                        case ACACIA_DOOR:
                        case DARK_OAK_DOOR:
                            if (!door && !(door = player.hasPermission(Perm.user_create_door))) {
                                return Result.DENY_BLOCK_PERM;
                            }
                            break;
                        case TRAP_DOOR:
                        case IRON_TRAPDOOR:
                            if (!trap && !(trap = player.hasPermission(Perm.user_create_trapdoor))) {
                                return Result.DENY_BLOCK_PERM;
                            }
                            break;
                        case FENCE_GATE:
                        case BIRCH_FENCE_GATE:
                        case ACACIA_FENCE_GATE:
                        case DARK_OAK_FENCE_GATE:
                        case JUNGLE_FENCE_GATE:
                        case SPRUCE_FENCE_GATE:
                            if (!gate && !(gate = player.hasPermission(Perm.user_create_fencegate))) {
                                return Result.DENY_BLOCK_PERM;
                            }
                            break;
                        case BREWING_STAND:
                            if (!brewery && !(brewery = player.hasPermission(Perm.user_create_brewery))) {
                                return Result.DENY_BLOCK_PERM;
                            }
                            break;
                        case CAULDRON:
                            if (!cauldron && !(cauldron = player.hasPermission(Perm.user_create_cauldron))) {
                                return Result.DENY_BLOCK_PERM;
                            }
                            break;
                        case ENCHANTMENT_TABLE:
                            if (!enchant && !(enchant = player.hasPermission(Perm.user_create_enchant))) {
                                return Result.DENY_BLOCK_PERM;
                            }
                            break;
                        case ENDER_CHEST:
                            if (!ender && !(ender = player.hasPermission(Perm.user_create_ender))) {
                                return Result.DENY_BLOCK_PERM;
                            }
                            break;
                        case ANVIL:
                            if (!anvil && !(anvil = player.hasPermission(Perm.user_create_anvil))) {
                                return Result.DENY_BLOCK_PERM;
                            }
                            break;
                        case BEACON:
                            if (!beacon && !(enchant = player.hasPermission(Perm.user_create_beacon))) {
                                return Result.DENY_BLOCK_PERM;
                            }
                            break;
                        case HOPPER:
                            if (!hopper && !(enchant = player.hasPermission(Perm.user_create_hopper))) {
                                return Result.DENY_BLOCK_PERM;
                            }
                            break;
                        case DROPPER:
                            if (!dropper && !(enchant = player.hasPermission(Perm.user_create_dropper))) {
                                return Result.DENY_BLOCK_PERM;
                            }
                            break;
                        case TRAPPED_CHEST:
                            if (!trappedChest && !(enchant = player.hasPermission(Perm.user_create_trapped_chest))) {
                                return Result.DENY_BLOCK_PERM;
                            }
                            break;

                    }
                }
                if (!chest && !dispenser && !furnace && !door && !trap && !gate && !brewery && !cauldron && !enchant && !hopper && !dropper && !trappedChest) {
                    //never found a valid block to protect
                    return Result.DENY_SIGN_PRIVATE_NOTHING_NEARBY;
                }
                return Result.SUCCESS;
            } else {
                //needs private first, pop sign
                return Result.DENY_SIGN_MOREUSERS_NO_PRIVATE;
            }
        }
    }
}
