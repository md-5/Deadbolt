package com.daemitus.deadbolt;

import java.util.UUID;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public final class UUIDs {
    private static final Pattern USER_NAME = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");

    public static boolean isName(String name) {
        return USER_NAME.matcher(name).matches();
    }

    public static OfflinePlayer getPlayer(String name) {
        return Bukkit.getOfflinePlayer(name);
    }

    public static UUID convertName(String name) {
        return getPlayer(name).getUniqueId();
    }

    public static String formatName(String name) {
        return format(convertName(name));
    }

    public static String format(OfflinePlayer player) {
        return format(player.getUniqueId());
    }

    public static String format(UUID uuid) {
        char[] buf = new char[8];
        long most = uuid.getMostSignificantBits(), least = uuid.getLeastSignificantBits();
        for (int i = 0; i < 4; i++) {
            buf[i] = (char) ((most >> (16*(4-i))) & 0xffff);
            buf[4+i] = (char) ((least >> (16*(4-i))) & 0xffff);
        }
        return new String(buf);
    }

    public static boolean validate(String line) {
        return line.length() == 8;
    }

    public static UUID get(String line) {
        Preconditions.checkArgument(validate(line), "Invalid UUID length");
        long most = 0, least = 0;
        for (int i = 0; i < 4; i++) {
            most |= (long) line.charAt(i) << (16*(4-i));
            least |= (long) line.charAt(4+i) << (16*(4-i));
        }
        return new UUID(most, least);
    }

    public static OfflinePlayer getPlayer(UUID uuid) {
        return Bukkit.getOfflinePlayer(uuid);
    }

    public static void deobfuscate(Sign sign, final Player player) {
        boolean isPrivate = Deadbolt.getLanguage().isPrivate(Util.getLine(sign, 0));
        boolean isMoreUsers = Deadbolt.getLanguage().isMoreUsers(Util.getLine(sign, 0));
        if (!(isPrivate || isMoreUsers)) return;
        final String[] display = sign.getLines().clone();
        for (int i = 1; i < 4; i++) {
            String line = Util.getLine(display, i);
            if (validate(line))
                display[i] = getPlayer(get(line)).getName();
        }
        final Location loc = sign.getLocation();
        Bukkit.getScheduler().runTaskLater(Deadbolt.getPlugin(), new Runnable() {
            @Override
            public void run() {
                player.sendSignChange(loc, display);
            }
        }, 5);
    }
}
