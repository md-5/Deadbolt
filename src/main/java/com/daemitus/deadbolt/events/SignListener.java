package com.daemitus.deadbolt.events;

import com.daemitus.deadbolt.Config;
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
import org.bukkit.plugin.PluginManager;

public final class SignListener extends org.bukkit.event.block.BlockListener {

    private final Deadbolt plugin;

    public SignListener(final Deadbolt plugin, final PluginManager pm) {
        this.plugin = plugin;
        pm.registerEvent(Type.SIGN_CHANGE, this, Priority.Normal, plugin);
    }

    private enum Result {

        DENY_SIGN_PRIVATE_ALREADY_OWNED, ADMIN_SIGN_PLACED, DENY_SIGN_MOREUSERS_ALREADY_OWNED, DENY_SIGN_PRIVATE_NOTHING_NEARBY, DENY_SIGN_MOREUSERS_NO_PRIVATE, SUCCESS, PLACEHOLDER, DENY_BLOCK_PERM_CHEST, DENY_BLOCK_PERM_FURNACE, DENY_BLOCK_PERM_DISPENSER, DENY_BLOCK_PERM_FENCEGATE, DENY_BLOCK_PERM_DOOR, DENY_BLOCK_PERM_TRAPDOOR;
    }

    @Override
    public void onSignChange(SignChangeEvent event) {
        if (event.isCancelled())
            return;
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String[] lines = event.getLines();

        if (Config.hasPermission(player, Perm.user_color))
            for (int i = 0; i < 4; i++)
                lines[i] = Config.createColor(lines[i]);

        String ident = Config.removeColor(lines[0]);
        boolean isPrivate = Config.isPrivate(ident);
        boolean isMoreUsers = Config.isMoreUsers(ident);
        if (!isPrivate && !isMoreUsers) {
            return;
        } else if (isPrivate) {
            for (int i = 0; i < 4; i++)
                lines[i] = Config.default_colors_private[i] + lines[i];
        } else if (isMoreUsers) {
            for (int i = 0; i < 4; i++)
                lines[i] = Config.default_colors_moreusers[i] + lines[i];
        }

        Deadbolted db = null;
        Result result = Result.PLACEHOLDER;
        if (block.getType().equals(Material.WALL_SIGN)) {
            Sign sign = (Sign) block.getState();
            sign.setLine(0, isPrivate ? Config.locale_private : Config.locale_moreusers);
            sign.update();
            db = Deadbolted.get(block);
            result = validateSignPlacement(db, player, isPrivate);
        } else {
            for (byte b = 0x2; b < 0x6 && !result.equals(Result.SUCCESS) && !result.equals(Result.ADMIN_SIGN_PLACED); b++) {
                block.setTypeIdAndData(Material.WALL_SIGN.getId(), b, false);
                Sign sign = (Sign) block.getState();
                sign.setLine(0, isPrivate ? Config.locale_private : Config.locale_moreusers);
                sign.update();
                db = Deadbolted.get(block);
                Result newresult = validateSignPlacement(db, player, isPrivate);
                if (!newresult.equals(Result.DENY_SIGN_PRIVATE_NOTHING_NEARBY))
                    result = newresult;
            }
        }

        switch (result) {
            case ADMIN_SIGN_PLACED:
                Config.sendMessage(player, ChatColor.RED, Config.msg_admin_sign_placed, db.getOwner());
            case SUCCESS:
                if (isPrivate) {
                    String owner = Config.removeColor(lines[1]);
                    if (owner.isEmpty())
                        lines[1] += Config.truncateName(owner);
                    else if (Config.hasPermission(player, Perm.admin_create) && plugin.getServer().getPlayer(owner) == null) {
                        Config.sendMessage(player, ChatColor.YELLOW, Config.msg_admin_warning_player_not_found, owner);
                    } else {
                        lines[1] += Config.truncateName(owner);
                    }
                }

                Sign sign = (Sign) block.getState();
                for (int i = 0; i < 4; i++)
                    sign.setLine(i, Config.formatForSign(lines[i]));
                sign.update();
                if (!ListenerManager.canSignChange(db, event))
                    break;
                return;
            case DENY_SIGN_PRIVATE_ALREADY_OWNED:
                Config.sendMessage(player, ChatColor.RED, Config.msg_deny_sign_private_already_owned);
                break;
            case DENY_SIGN_MOREUSERS_ALREADY_OWNED:
                Config.sendMessage(player, ChatColor.RED, Config.msg_deny_sign_moreusers_already_owned);
                break;
            case DENY_SIGN_MOREUSERS_NO_PRIVATE:
                Config.sendMessage(player, ChatColor.RED, Config.msg_deny_sign_moreusers_no_private);
                break;
            case DENY_BLOCK_PERM_CHEST:
                Config.sendMessage(player, ChatColor.RED, Config.msg_deny_block_perm, "chests");
                break;
            case DENY_BLOCK_PERM_DISPENSER:
                Config.sendMessage(player, ChatColor.RED, Config.msg_deny_block_perm, "dispensers");
                break;
            case DENY_BLOCK_PERM_FURNACE:
                Config.sendMessage(player, ChatColor.RED, Config.msg_deny_block_perm, "furnaces");
                break;
            case DENY_BLOCK_PERM_DOOR:
                Config.sendMessage(player, ChatColor.RED, Config.msg_deny_block_perm, "doors");
                break;
            case DENY_BLOCK_PERM_TRAPDOOR:
                Config.sendMessage(player, ChatColor.RED, Config.msg_deny_block_perm, "trapdoors");
                break;
            case DENY_BLOCK_PERM_FENCEGATE:
                Config.sendMessage(player, ChatColor.RED, Config.msg_deny_block_perm, "fencegates");
                break;
            default:
                //case DENY_SIGN_PRIVATE_NOTHING_NEARBY:
                Config.sendMessage(player, ChatColor.RED, Config.msg_deny_sign_private_nothing_nearby);
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
                    if (Config.hasPermission(player, Perm.admin_create)) {
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
                for (Block setBlock : db.getBlocks()) {
                    //not authorized to protect?
                    switch (setBlock.getType()) {
                        case CHEST:
                            if (!chest && !(chest = Config.hasPermission(player, Perm.user_create_chest)))
                                return Result.DENY_BLOCK_PERM_CHEST;
                            break;
                        case DISPENSER:
                            if (!dispenser && !(dispenser = Config.hasPermission(player, Perm.user_create_dispenser)))
                                return Result.DENY_BLOCK_PERM_DISPENSER;
                            break;
                        case FURNACE:
                        case BURNING_FURNACE:
                            if (!furnace && !(furnace = Config.hasPermission(player, Perm.user_create_furnace)))
                                return Result.DENY_BLOCK_PERM_FURNACE;
                            break;
                        case WOODEN_DOOR:
                        case IRON_DOOR_BLOCK:
                            if (!door && !(door = Config.hasPermission(player, Perm.user_create_door)))
                                return Result.DENY_BLOCK_PERM_DOOR;
                            break;
                        case TRAP_DOOR:
                            if (!trap && !(trap = Config.hasPermission(player, Perm.user_create_trapdoor)))
                                return Result.DENY_BLOCK_PERM_TRAPDOOR;
                            break;
                        case FENCE_GATE:
                            if (!gate && !(gate = Config.hasPermission(player, Perm.user_create_fencegate)))
                                return Result.DENY_BLOCK_PERM_FENCEGATE;
                            break;

                    }
                }
                if (!chest && !dispenser && !furnace && !door && !trap && !gate) {
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
