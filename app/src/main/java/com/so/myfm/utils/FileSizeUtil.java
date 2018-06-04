package com.so.myfm.utils;

/**
 *
 * Created by so on 2018/1/18.
 */

public class FileSizeUtil {
    public static String generateSize(long size) {
        long result = size;
        long gb = 2 << 29;
        long mb = 2 << 19;
        long kb = 2 << 9;
        if (result < kb) {
            return result + "B";
        } else if (result >= kb && result < mb) {
            return String.format("%.2fKB", result / (double) kb);
        } else if (result >= mb && result < gb) {
            return String.format("%.2fMB", result / (double) mb);
        } else if (result >= gb) {
            return String.format("%.2fGB", result / (double) gb);
        }
        return null;
    }
}
