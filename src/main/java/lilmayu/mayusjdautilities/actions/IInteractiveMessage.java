package lilmayu.mayusjdautilities.actions;

import lilmayu.mayusjdautilities.actions.evenets.InteractionEvent;

public interface IInteractiveMessage {

    /**
     * @return TRUE = success, delete message from listener; FALSE = dont delete message from listener
     */
    boolean process(InteractionEvent interactionEvent);

}
