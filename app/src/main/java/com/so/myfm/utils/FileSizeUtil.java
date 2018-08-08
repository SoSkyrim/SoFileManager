package com.so.myfm.utils;

import java.util.Locale;

/**
 *
 * Created by so on 2018/1/18.
 */

public class FileSizeUtil {
    public static String generateSize(long size) {
        long gb = 2 << 29;
        long mb = 2 << 19;
        long kb = 2 << 9;
        if (size < kb) {
            return size + "B";
        } else if (size >= kb && size < mb) {
            return String.format(Locale.CHINA, "%.2fKB", size / (double) kb);
        } else if (size >= mb && size < gb) {
            return String.format(Locale.CHINA, "%.2fMB", size / (double) mb);
        } else if (size >= gb) {
            return String.format(Locale.CHINA, "%.2fGB", size / (double) gb);
        }
        return null;
    }
}
