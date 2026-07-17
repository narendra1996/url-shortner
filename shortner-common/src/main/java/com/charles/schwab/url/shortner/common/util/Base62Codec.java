package com.charles.schwab.url.shortner.common.util;

public class Base62Codec {
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = ALPHABET.length();

    public static String encode(long id) {
        if (id < 0) {
            throw new IllegalArgumentException("ID must be non-negative");
        }
        if (id == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(ALPHABET.charAt((int) (id % BASE)));
            id /= BASE;
        }
        return sb.reverse().toString();
    }

    public static long decode(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("String must not be empty");
        }
        long num = 0;
        for (int i = 0; i < str.length(); i++) {
            int index = ALPHABET.indexOf(str.charAt(i));
            if (index == -1) {
                throw new IllegalArgumentException("Invalid character in Base62 string: " + str.charAt(i));
            }
            num = num * BASE + index;
        }
        return num;
    }
}
