package com.daemitus.deadbolt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewClass {

    private static final Pattern STRIPCOLOR = Pattern.compile("§[0-9a-fA-F]");
    private static final Pattern STRIPTWOCOLORS = Pattern.compile("(§[0-9a-fA-F])(\\s*)(§[0-9a-fA-F])");

    public static void main(String[] args) {
        int i = 0;
        Matcher m = STRIPCOLOR.matcher("§1§2§3");
        System.out.println(m.find());
        System.out.println(m.find());
        System.out.println(m.find());
        System.out.println(m.find());
    }
}
