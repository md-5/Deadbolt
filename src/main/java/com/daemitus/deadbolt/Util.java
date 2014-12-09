package com.daemitus.deadbolt;

import java.io.*;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import org.apache.commons.io.FilenameUtils;

public final class Util {

    protected static final String patternBracketTooLong = "\\[.{14,}\\]";
    private static final Pattern PSEUDO_COLOR = Pattern.compile("\\&([0-9a-f])");

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
        if (text == null) {
            return null;
        }
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

    public static void extractLibraries(String listenerFile, String dest){
        try {
            String home = DeadboltPlugin.class.getProtectionDomain().
                    getCodeSource().getLocation().toString().
                    substring(6);
            JarFile jar = new JarFile(home);
            ZipEntry entry = jar.getEntry(listenerFile);

            String inFileName = FilenameUtils.getBaseName(entry.getName())+".class";
            File efile = new File(dest, inFileName);
            InputStream in =
                    new BufferedInputStream(jar.getInputStream(entry));
            OutputStream out =
                    new BufferedOutputStream(new FileOutputStream(efile));
            byte[] buffer = new byte[2048];
            for (;;)  {
                int nBytes = in.read(buffer);
                if (nBytes <= 0) break;
                out.write(buffer, 0, nBytes);
            }
            out.flush();
            out.close();
            in.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
