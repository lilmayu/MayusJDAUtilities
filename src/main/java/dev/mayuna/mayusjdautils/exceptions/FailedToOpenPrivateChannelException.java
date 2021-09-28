package dev.mayuna.mayusjdautils.exceptions;

import lombok.Getter;
import net.dv8tion.jda.api.entities.User;

public class FailedToOpenPrivateChannelException extends RuntimeException {

    private final @Getter User user;

    public FailedToOpenPrivateChannelException(RuntimeException runtimeException, User user) {
        super(runtimeException);
        this.user = user;
    }
}
