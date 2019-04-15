package de.kaleidox.jumpcube.exception;

public final class DuplicateCubeException extends InnerCommandException {
    public DuplicateCubeException(String name) {
        super("Cube duplicate found with name \"" + name + "\"");
    }
}
