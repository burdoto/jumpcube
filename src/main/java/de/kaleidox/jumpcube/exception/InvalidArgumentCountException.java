package de.kaleidox.jumpcube.exception;

import de.kaleidox.jumpcube.chat.MessageLevel;

public final class InvalidArgumentCountException extends InnerCommandException {
    @Override
    public String getIngameText() {
        return getMessage();
    }

    public InvalidArgumentCountException(int expected, int actual) {
        super(MessageLevel.ERROR, String.format("Too %s arguments! Expected: %d",
                (actual < expected ? "few" : "many"), expected));
    }
}
