package org.cubewhy.launcher.utils;

public class StringUtils {
    public static int count(String input, char target) {
        int c = 0;
        char[] chars = input.toCharArray();
        for (char c1 :
                chars) {
            if (c1 == target) {
                c++;
            }
        }
        return c;
    }
}
