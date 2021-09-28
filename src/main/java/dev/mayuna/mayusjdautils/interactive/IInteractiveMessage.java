package dev.mayuna.mayusjdautils.interactive;

import dev.mayuna.mayusjdautils.interactive.evenets.InteractionEvent;

public interface IInteractiveMessage {

    /**
     * @return TRUE = success, delete message from listener; FALSE = dont delete message from listener
     */
    boolean process(InteractionEvent interactionEvent);

}
