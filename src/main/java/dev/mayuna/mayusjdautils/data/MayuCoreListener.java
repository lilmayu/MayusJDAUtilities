package dev.mayuna.mayusjdautils.data;

import dev.mayuna.mayusjdautils.interactive.InteractiveMessage;
import dev.mayuna.mayusjdautils.interactive.evenets.InteractionEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MayuCoreListener extends ListenerAdapter {

    private final static List<InteractiveMessage> intractableMessageList = new ArrayList<>();

    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getUser() == null) {
            return;
        }

        if (event.getUser().isBot()) {
            return;
        }

        InteractionEvent interactionEvent = new InteractionEvent(event);
        processEvents(interactionEvent);
    }

    public void onButtonClick(ButtonClickEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        event.getInteraction().deferEdit().complete();
        InteractionEvent interactionEvent = new InteractionEvent(event);
        processEvents(interactionEvent);
    }

    public void onSelectionMenu(SelectionMenuEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        event.getInteraction().deferEdit().complete();
        InteractionEvent interactionEvent = new InteractionEvent(event);
        processEvents(interactionEvent);
    }

    private void processEvents(InteractionEvent interactionEvent) {
        List<InteractiveMessage> intractableMessageListCopy = new ArrayList<>(intractableMessageList);

        for (InteractiveMessage intractableMessage : intractableMessageListCopy) {
            if (interactionEvent.isReactionInteraction() && intractableMessage.isMessage(interactionEvent.getInteractedMessageID())) {
                MessageReactionAddEvent event = interactionEvent.getReactionAddEvent();

                if (event != null && event.getUser() != null) {
                    event.getReaction().removeReaction(event.getUser()).complete();
                }
            }

            boolean success = intractableMessage.process(interactionEvent);

            if (success) {
                if (intractableMessage.isDeleteMessageAfterInteraction()) {
                    try {
                        intractableMessage.getMessage().delete().complete();
                    } catch (ErrorResponseException ignored) {
                    }
                    removeIntractableMessage(intractableMessage);
                }
                return;
            }
        }
    }

    public static void addIntractableMessage(InteractiveMessage intractableMessage) {
        intractableMessageList.add(intractableMessage);
    }

    public static void removeIntractableMessage(InteractiveMessage intractableMessage) {
        intractableMessageList.remove(intractableMessage);
    }
}
