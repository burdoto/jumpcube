package de.kaleidox.jumpcube.exception;

import de.kaleidox.jumpcube.chat.MessageLevel;

public final class GameRunningException extends InnerCommandException {
    public GameRunningException(String message) {
        super(MessageLevel.ERROR, message);
    }
}
