package com.daemitus.deadbolt;

import java.util.regex.Pattern;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

public final class Util {

    protected static final String patternBracketTooLong = "\\[.{14,}\\]";
    private static final Pattern DETECT_COLORS = Pattern.compile("\u00A7([0-9a-f])");
    private static final Pattern TWO_COLORS = Pattern.compile("(\u00A7[0-9a-f])(\\s*)(§[0-9a-f])");
    private static final Pattern PSEUDO_COLOR = Pattern.compile("\\&([0-9a-f])");
    private static final Pattern UNNEEDED_COLOR = Pattern.compile("^\u00A70");
    private static final Pattern FORMAT_LENGTH = Pattern.compile("(^.{0,15}).*");

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

    public static String truncateName(String name) {
        return name.substring(0, name.length() > 13 ? 13 : name.length());
    }

    public static Block getSignAttached(Sign signState) {
        return signState.getBlock().getRelative(((org.bukkit.material.Sign) signState.getData()).getAttachedFace());
    }

    public static String formatForSign(String line) {
        while (UNNEEDED_COLOR.matcher(line).find()) {
            line = UNNEEDED_COLOR.matcher(line).replaceAll("");
        }
        while (TWO_COLORS.matcher(line).find()) {
            line = TWO_COLORS.matcher(line).replaceAll("$2$3");
        }
        line = FORMAT_LENGTH.matcher(line).replaceAll("$1");
        line = line.substring(0, line.length() > 15 ? 15 : line.length());
        return line;
    }

    public static String removeColor(String text) {
        return text == null ? null : DETECT_COLORS.matcher(text).replaceAll("");
    }

    public static String createColor(String text) {
        return text == null ? null : PSEUDO_COLOR.matcher(text).replaceAll("\u00A7$1");
    }

    public static String getLine(Sign signBlock, int line) {
        return DETECT_COLORS.matcher(signBlock.getLine(line)).replaceAll("");
    }

    public static String truncate(String text) {
        if (text.matches(patternBracketTooLong)) {
            return "[" + text.substring(1, 14) + "]";
        }
        return text;
    }
}
