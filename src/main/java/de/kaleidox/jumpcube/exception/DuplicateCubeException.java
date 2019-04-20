package de.kaleidox.jumpcube.exception;

public final class DuplicateCubeException extends InnerCommandException {
    public DuplicateCubeException(String name) {
        super("Cube names must be unique! [" + name + "]");
    }
}
