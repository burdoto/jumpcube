package de.kaleidox.jumpcube.util;

import org.bukkit.Location;
import org.bukkit.World;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public final class WorldUtil {
    private WorldUtil() {
    }

    public static double dist(int[] pos1, int[] pos2) {
        return sqrt(pow(pos2[0] - pos1[0], 2) + pow(pos2[2] - pos1[2], 2));
    }

    public static int[] mid(int[][] pos) {
        return new int[]{
                MathUtil.mid(pos[0][0], pos[1][0]),
                MathUtil.mid(pos[0][1], pos[1][1]),
                MathUtil.mid(pos[0][2], pos[1][2])
        };
    }

    public static int[] xyz(Location location) {
        return new int[]{location.getBlockX(), location.getBlockY(), location.getBlockZ()};
    }

    public static Location location(World world, int[] xyz) {
        return world.getBlockAt(xyz[0], xyz[1], xyz[2]).getLocation();
    }
}
