package de.kaleidox.jumpcube.cube;

import org.bukkit.World;

public interface Cube {
    String getCubeName();

    int[][] getPositions();

    int getHeight();

    BlockBar getBlockBar();

    World getWorld();

    void delete();
}
