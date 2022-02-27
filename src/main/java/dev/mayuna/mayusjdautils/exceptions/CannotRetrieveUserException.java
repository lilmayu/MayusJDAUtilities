package dev.mayuna.mayusjdautils.exceptions;

import lombok.Getter;

public class CannotRetrieveUserException extends RuntimeException {

    private final @Getter Throwable throwable;
    private final @Getter long rawUserID;

    public CannotRetrieveUserException(Throwable throwable, long rawUserID) {
        super("Cannot retrieve user with ID " + rawUserID + "!", throwable);
        this.throwable = throwable;
        this.rawUserID = rawUserID;
    }
}
