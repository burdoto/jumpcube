package de.kaleidox.jumpcube.cube;

import org.bukkit.World;

public interface Cube {
    String getCubeName();

    void delete();

    int[][] getPositions();

    BlockBar getBlockBar();

    World getWorld();
}
