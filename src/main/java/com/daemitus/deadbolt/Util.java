package com.daemitus.deadbolt;

import java.util.UUID;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

public final class Util {

    public static int blockFaceToNotch(BlockFace face) {
        switch (face) {
            case DOWN:
                return 0;
            case UP:
                return 1;
            case NORTH:
                return 2;
            case SOUTH:
                return 3;
            case WEST:
                return 4;
            case EAST:
                return 5;
            default:
                return 7; // Good as anything here, but technically invalid
        }
    }

    public static String formatForSign(String line) {
        // Bukkit does already verify the max length of the line
        return removeColor(line);
    }

    public static Block getSignAttached(Sign signState) {
        return signState.getBlock().getRelative(((org.bukkit.material.Sign) signState.getData()).getAttachedFace());
    }

    public static String removeColor(String text) {
        if (text == null) {
            return null;
        }
        return ChatColor.stripColor(text);
    }

    public static String getLine(Sign signBlock, int line) {
        return removeColor(signBlock.getLine(line));
    }

    public static String getLine(String[] lines, int line) {
        return removeColor(lines[line]);
    }
}
