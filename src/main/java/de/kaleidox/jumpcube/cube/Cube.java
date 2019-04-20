package de.kaleidox.jumpcube.cube;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public interface Cube {
    String getCubeName();

    void delete();

    int[][] getPositions();

    BlockBar getBlockBar();

    static double dist(int[][] pos) {
        return sqrt(pow(pos[1][0] - pos[0][0], 2) + pow(pos[1][2] - pos[0][2], 2));
    }
}
