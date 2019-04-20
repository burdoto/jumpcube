package de.kaleidox.jumpcube.util;

public final class MathUtil {
    private MathUtil() {
    }

    public static int mid(int x, int y) {
        return x / 2 + y / 2 + (x % 2 + y % 2) / 2;
    }
}
