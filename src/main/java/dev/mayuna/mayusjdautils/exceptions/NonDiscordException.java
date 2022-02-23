package dev.mayuna.mayusjdautils.exceptions;

public class NonDiscordException extends RuntimeException {

    public NonDiscordException(Throwable throwable) {
        super("Non Discord exception has occurred!", throwable);
    }
}
