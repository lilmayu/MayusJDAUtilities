package lilmayu.mayusjdautilities.exceptions;

import lilmayu.mayusjdautilities.actions.objects.MessageInteraction;
import lombok.Getter;

public class CannotAddInteractionException extends RuntimeException {

    private final @Getter MessageInteraction messageInteraction;

    public CannotAddInteractionException(String message, MessageInteraction messageInteraction) {
        super(message);
        this.messageInteraction = messageInteraction;
    }
}
