package de.kaleidox.jumpcube.cube;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

public class ExistingCube implements Cube {
    private final static Map<String, Cube> instances = new ConcurrentHashMap<>();

    @Override
    public String getCubeName() {
        return null;
    }

    @Override
    public void delete() {

    }

    @Override
    public Location[] getPositions() {
        return null;
    }

    @Nullable
    public static ExistingCube get(String name) {
        return null;
    }

    public static boolean exists(String name) {
        return instances.containsKey(name);
    }
}
