package com.daemitus.deadbolt;

import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

public final class Util {

    protected static final String patternBracketTooLong = "\\[.{14,}\\]";
    private static final Pattern PSEUDO_COLOR = Pattern.compile("\\&([0-9a-f])");

    public static BlockFace getFacingFromByte(byte b) {
        switch (b) {
            case 0x2:
                return BlockFace.EAST;
            case 0x3:
                return BlockFace.WEST;
            case 0x4:
                return BlockFace.NORTH;
            case 0x5:
                return BlockFace.SOUTH;
            default:
                return null;
        }
    }

    public static byte getByteFromFacing(BlockFace bf) {
        switch (bf) {
            case EAST:
                return 0x2;
            case WEST:
                return 0x3;
            case NORTH:
                return 0x4;
            case SOUTH:
                return 0x5;
            default:
                return 0x0;
        }
    }
    
    public static String formatForSign(String line, int maxlen) {
    	line = removeColor(line);
    	line = line.substring(0, line.length() > maxlen ? maxlen : line.length());
        return line;
    }
    
    public static String formatForSign(String line) {
    	return formatForSign(line, 15);
    }
    
    public static boolean signNameEqualsPlayerName(String signName, String playerName) {
    	String playerName15 = formatForSign(playerName);
    	
    	if (signName.equalsIgnoreCase(playerName15)) {
    		return true;
    	}
    	
    	return false;
    }

    public static Block getSignAttached(Sign signState) {
        return signState.getBlock().getRelative(((org.bukkit.material.Sign) signState.getData()).getAttachedFace());
    }

    public static String removeColor(String text) {
    	if (text == null) return null;
    	return ChatColor.stripColor(text);
    }

    public static String createColor(String text) {
        return text == null ? null : PSEUDO_COLOR.matcher(text).replaceAll("\u00A7$1");
    }

    public static String getLine(Sign signBlock, int line) {
        return removeColor(signBlock.getLine(line));
    }

    public static String truncate(String text) {
        if (text.matches(patternBracketTooLong)) {
            return "[" + text.substring(1, 14) + "]";
        }
        return text;
    }
}
