package de.kaleidox.jumpcube.game.listener;

import de.kaleidox.jumpcube.cube.Cube;

public abstract class ListenerBase {
    protected final Cube cube;

    protected ListenerBase(Cube cube) {
        this.cube = cube;
    }
}
