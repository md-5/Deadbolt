package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Deadbolt;
import com.daemitus.deadbolt.Deadbolted;
import com.daemitus.deadbolt.Perm;
import com.daemitus.deadbolt.listener.ListenerManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

public final class SignListener extends org.bukkit.event.block.BlockListener {

    private final Deadbolt plugin = Deadbolt.instance;

    public SignListener() {
        plugin.getServer().getPluginManager().registerEvent(Type.SIGN_CHANGE, this, Priority.Normal, plugin);
    }

    private enum Result {

        DENY_SIGN_PRIVATE_ALREADY_OWNED, ADMIN_SIGN_PLACED, DENY_SIGN_MOREUSERS_ALREADY_OWNED, DENY_SIGN_PRIVATE_NOTHING_NEARBY, DENY_SIGN_MOREUSERS_NO_PRIVATE, SUCCESS, PLACEHOLDER, DENY_BLOCK_PERM_CHEST, DENY_BLOCK_PERM_FURNACE, DENY_BLOCK_PERM_DISPENSER, DENY_BLOCK_PERM_FENCEGATE, DENY_BLOCK_PERM_DOOR, DENY_BLOCK_PERM_TRAPDOOR, DENY_BLOCK_PERM_BREWERY, DENY_BLOCK_PERM_CAULDRON, DENY_BLOCK_PERM_ENCHANT;
    }

    @Override
    public void onSignChange(SignChangeEvent event) {
        if (event.isCancelled())
            return;
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String[] lines = event.getLines();

        if (plugin.config.hasPermission(player, Perm.user_color))
            for (int i = 0; i < 4; i++)
                lines[i] = plugin.config.createColor(lines[i]);

        //fix for clientside sign edit hack
        if (event.getBlock().getType().equals(Material.WALL_SIGN)) {
            Sign sign = (Sign) event.getBlock().getState();
            String ident = plugin.config.getLine(sign, 0);
            if (plugin.config.isPrivate(ident) || plugin.config.isMoreUsers(ident)) {
                event.setCancelled(true);
                return;
            }
        }
        //fix end

        String ident = plugin.config.removeColor(lines[0]);
        boolean isPrivate = plugin.config.isPrivate(ident);
        boolean isMoreUsers = plugin.config.isMoreUsers(ident);
        if (!isPrivate && !isMoreUsers) {
            return;
        } else if (isPrivate) {
            for (int i = 0; i < 4; i++)
                lines[i] = plugin.config.default_colors_private[i] + lines[i];
        } else if (isMoreUsers) {
            for (int i = 0; i < 4; i++)
                lines[i] = plugin.config.default_colors_moreusers[i] + lines[i];
        }

        Deadbolted db = null;
        Result result = Result.PLACEHOLDER;
        if (block.getType().equals(Material.WALL_SIGN)) {
            Sign sign = (Sign) block.getState();
            sign.setLine(0, isPrivate ? plugin.config.locale_private : plugin.config.locale_moreusers);
            sign.update();
            db = Deadbolted.get(block);
            result = validateSignPlacement(db, player, isPrivate);
        } else {
            for (byte b = 0x2; b < 0x6 && !result.equals(Result.SUCCESS) && !result.equals(Result.ADMIN_SIGN_PLACED); b++) {
                block.setTypeIdAndData(Material.WALL_SIGN.getId(), b, false);
                Sign sign = (Sign) block.getState();
                sign.setLine(0, isPrivate ? plugin.config.locale_private : plugin.config.locale_moreusers);
                sign.update();
                db = Deadbolted.get(block);
                Result newresult = validateSignPlacement(db, player, isPrivate);
                if (!newresult.equals(Result.DENY_SIGN_PRIVATE_NOTHING_NEARBY))
                    result = newresult;
            }
        }

        switch (result) {
            case ADMIN_SIGN_PLACED:
                plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_admin_sign_placed, db.getOwner());
            case SUCCESS:
                if (isPrivate) {
                    String owner = plugin.config.removeColor(lines[1]);
                    if (owner.isEmpty()) {
                        lines[1] = plugin.config.default_colors_private[1] + plugin.config.truncateName(player.getName());
                    } else if (plugin.config.hasPermission(player, Perm.admin_create)) {
                        if (plugin.getServer().getPlayerExact(owner) == null)
                            plugin.config.sendMessage(player, ChatColor.YELLOW, plugin.config.msg_admin_warning_player_not_found, owner);
                    } else {
                        lines[1] = plugin.config.default_colors_private[1] + plugin.config.truncateName(player.getName());
                    }
                }

                Sign sign = (Sign) block.getState();
                for (int i = 0; i < 4; i++)
                    sign.setLine(i, plugin.config.formatForSign(lines[i]));
                sign.update();
                if (!ListenerManager.canSignChange(db, event))
                    break;
                return;
            case DENY_SIGN_PRIVATE_ALREADY_OWNED:
                plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_sign_private_already_owned);
                break;
            case DENY_SIGN_MOREUSERS_ALREADY_OWNED:
                plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_sign_moreusers_already_owned);
                break;
            case DENY_SIGN_MOREUSERS_NO_PRIVATE:
                plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_sign_moreusers_no_private);
                break;
            case DENY_BLOCK_PERM_CHEST:
                plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_block_perm, "chests");
                break;
            case DENY_BLOCK_PERM_DISPENSER:
                plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_block_perm, "dispensers");
                break;
            case DENY_BLOCK_PERM_FURNACE:
                plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_block_perm, "furnaces");
                break;
            case DENY_BLOCK_PERM_DOOR:
                plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_block_perm, "doors");
                break;
            case DENY_BLOCK_PERM_TRAPDOOR:
                plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_block_perm, "trapdoors");
                break;
            case DENY_BLOCK_PERM_FENCEGATE:
                plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_block_perm, "fencegates");
                break;
            case DENY_BLOCK_PERM_BREWERY:
                plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_block_perm, "brewing stands");
                break;
            case DENY_BLOCK_PERM_CAULDRON:
                plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_block_perm, "cauldrons");
                break;
            case DENY_BLOCK_PERM_ENCHANT:
                plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_block_perm, "enchantment tables");
                break;
            default:
                //case DENY_SIGN_PRIVATE_NOTHING_NEARBY:
                plugin.config.sendMessage(player, ChatColor.RED, plugin.config.msg_deny_sign_private_nothing_nearby);
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
                    if (plugin.config.hasPermission(player, Perm.admin_create)) {
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
                for (Block setBlock : db.getBlocks()) {
                    //not authorized to protect?
                    switch (setBlock.getType()) {
                        case CHEST:
                            if (!chest && !(chest = plugin.config.hasPermission(player, Perm.user_create_chest)))
                                return Result.DENY_BLOCK_PERM_CHEST;
                            break;
                        case DISPENSER:
                            if (!dispenser && !(dispenser = plugin.config.hasPermission(player, Perm.user_create_dispenser)))
                                return Result.DENY_BLOCK_PERM_DISPENSER;
                            break;
                        case FURNACE:
                        case BURNING_FURNACE:
                            if (!furnace && !(furnace = plugin.config.hasPermission(player, Perm.user_create_furnace)))
                                return Result.DENY_BLOCK_PERM_FURNACE;
                            break;
                        case WOODEN_DOOR:
                        case IRON_DOOR_BLOCK:
                            if (!door && !(door = plugin.config.hasPermission(player, Perm.user_create_door)))
                                return Result.DENY_BLOCK_PERM_DOOR;
                            break;
                        case TRAP_DOOR:
                            if (!trap && !(trap = plugin.config.hasPermission(player, Perm.user_create_trapdoor)))
                                return Result.DENY_BLOCK_PERM_TRAPDOOR;
                            break;
                        case FENCE_GATE:
                            if (!gate && !(gate = plugin.config.hasPermission(player, Perm.user_create_fencegate)))
                                return Result.DENY_BLOCK_PERM_FENCEGATE;
                            break;
                        case BREWING_STAND:
                            if (!brewery && !(brewery = plugin.config.hasPermission(player, Perm.user_create_brewery)))
                                return Result.DENY_BLOCK_PERM_BREWERY;
                            break;
                        case CAULDRON:
                            if (!cauldron && !(cauldron = plugin.config.hasPermission(player, Perm.user_create_cauldron)))
                                return Result.DENY_BLOCK_PERM_CAULDRON;
                            break;
                        case ENCHANTMENT_TABLE:
                            if (!enchant && !(enchant = plugin.config.hasPermission(player, Perm.user_create_enchant)))
                                return Result.DENY_BLOCK_PERM_ENCHANT;
                            break;


                    }
                }
                if (!chest && !dispenser && !furnace && !door && !trap && !gate && !brewery && !cauldron && !enchant) {
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