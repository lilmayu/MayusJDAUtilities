package dev.mayuna.mayusjdautils.exceptions;

import dev.mayuna.mayusjdautils.interactive.objects.MessageInteraction;
import lombok.Getter;

public class CannotAddInteractionException extends RuntimeException {

    private final @Getter MessageInteraction messageInteraction;

    public CannotAddInteractionException(String message, MessageInteraction messageInteraction) {
        super(message);
        this.messageInteraction = messageInteraction;
    }
}
