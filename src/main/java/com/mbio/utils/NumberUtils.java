package com.mbio.utils;

/**
 * Converts values shown on the page into numbers.
 */
public final class NumberUtils {

    private NumberUtils() {
    }

    /** Examples: "$ 78,687.82" -> 78687.82 , "2.06%" -> 2.06 , "BTC 20,084,256" -> 20084256 */
    public static double parseNumber(String text) {
        String digitsOnly = text.replaceAll("[^0-9.\\-]", "");
        return digitsOnly.isEmpty() ? 0 : Double.parseDouble(digitsOnly);
    }
}
