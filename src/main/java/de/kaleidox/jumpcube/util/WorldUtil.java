package de.kaleidox.jumpcube.util;

import org.bukkit.Location;
import org.bukkit.World;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public final class WorldUtil {
    public static double dist(int[][] pos) {
        return sqrt(pow(pos[1][0] - pos[0][0], 2) + pow(pos[1][2] - pos[0][2], 2));
    }

    public static int[] xyz(Location location) {
        return new int[]{location.getBlockX(), location.getBlockY(), location.getBlockZ()};
    }

    public static Location getLocation(World world, int[] xyz) {
        return world.getBlockAt(xyz[0], xyz[1], xyz[2]).getLocation();
    }
}
