package de.kaleidox.jumpcube.cube;

import org.bukkit.Location;

public interface Cube {
    String getCubeName();

    void delete();

    Location[] getPositions();
}
