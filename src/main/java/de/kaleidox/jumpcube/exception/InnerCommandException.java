package de.kaleidox.jumpcube.exception;

import de.kaleidox.jumpcube.chat.MessageLevel;

import static de.kaleidox.jumpcube.chat.MessageLevel.EXCEPTION;

public abstract class InnerCommandException extends RuntimeException {
    private final MessageLevel level;

    public MessageLevel getLevel() {
        return level;
    }

    public String getIngameText() {
        return "[" + getClass().getSimpleName() + "] " + getMessage();
    }

    public InnerCommandException() {
        super();
        this.level = EXCEPTION;
    }

    public InnerCommandException(String message) {
        super(message);
        this.level = EXCEPTION;
    }

    public InnerCommandException(String message, Throwable cause) {
        super(message, cause);
        this.level = EXCEPTION;
    }

    public InnerCommandException(Throwable cause) {
        super(cause);
        this.level = EXCEPTION;
    }

    public InnerCommandException(MessageLevel level) {
        super();
        this.level = level;
    }

    public InnerCommandException(MessageLevel level, String message) {
        super(message);
        this.level = level;
    }

    public InnerCommandException(MessageLevel level, String message, Throwable cause) {
        super(message, cause);
        this.level = level;
    }

    public InnerCommandException(MessageLevel level, Throwable cause) {
        super(cause);
        this.level = level;
    }
}
