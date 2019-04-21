package de.kaleidox.jumpcube.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static de.kaleidox.jumpcube.util.MathUtil.raising;

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

    public static boolean inside(int[][] area, int[] xyz) {
        return raising(min(area[0][0], area[1][0]), xyz[0], max(area[0][0], area[1][0]))
                && raising(min(area[0][1], area[1][2]), xyz[1], max(area[0][1], area[1][2]))
                && raising(min(area[0][2], area[1][2]), xyz[2], max(area[0][2], area[1][2]));
    }

    public static int[] xyz(Location location) {
        return new int[]{location.getBlockX(), location.getBlockY(), location.getBlockZ()};
    }

    public static Location location(World world, int[] xyz) {
        return world.getBlockAt(xyz[0], xyz[1], xyz[2]).getLocation();
    }

    @Contract(mutates = "param1")
    public static int[][] expandVert(int[][] positions) {
        positions[0][1] = 0;
        positions[1][1] = 256;
        return positions;
    }
}
