package dev.mayuna.mayusjdautils.exceptions;

import dev.mayuna.mayusjdautils.interactive.Interaction;
import lombok.Getter;

public class CannotAddInteractionException extends RuntimeException {

    private final @Getter Interaction interaction;

    public CannotAddInteractionException(String message, Interaction interaction) {
        super(message);
        this.interaction = interaction;
    }
}
