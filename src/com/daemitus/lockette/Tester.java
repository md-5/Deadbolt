package com.daemitus.lockette;

public class Tester {

    public static void main(String[] args) {
        String pattern =  "\\[.{1,11}:[123]\\]";
        System.out.println("[Timer:1]".matches(pattern));
    }
}
