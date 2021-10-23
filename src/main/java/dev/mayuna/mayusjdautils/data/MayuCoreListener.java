package dev.mayuna.mayusjdautils.data;

import dev.mayuna.mayusjdautils.interactive.InteractiveMessage;
import dev.mayuna.mayusjdautils.interactive.evenets.InteractionEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenuInteraction;

import java.util.ArrayList;
import java.util.List;

public class MayuCoreListener extends ListenerAdapter {

    private final static List<InteractiveMessage> intractableMessageList = new ArrayList<>();

    public static boolean enableExperimentalInteractionBehavior = false;
    public static final String GENERIC_BUTTON_CLOSE_ID = "generic_close_button";

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

        boolean processed = false;

        for (InteractiveMessage intractableMessage : intractableMessageListCopy) {
            if (interactionEvent.isReactionInteraction() && intractableMessage.isMessage(interactionEvent.getInteractedMessageID())) {
                MessageReactionAddEvent event = interactionEvent.getReactionAddEvent();

                if (event != null && event.getUser() != null) {
                    event.getReaction().removeReaction(event.getUser()).complete();
                }
            }

            boolean success = intractableMessage.process(interactionEvent);

            if (success) {
                processed = true;
                if (intractableMessage.isDeleteMessageAfterInteraction()) {
                    intractableMessage.delete();
                }
                break;
            }
        }

        if (enableExperimentalInteractionBehavior) {
            if (!processed) {
                if (interactionEvent.isButtonInteraction()) {
                    ButtonInteraction buttonInteraction = interactionEvent.getButtonClickEvent();
                    Button button = buttonInteraction.getButton();

                    if (button != null) {
                        if (button.getId() != null) {
                            if (button.getId().equals(GENERIC_BUTTON_CLOSE_ID)) {
                                interactionEvent.getButtonClickEvent().getMessage().delete().queue(success -> {}, failure -> {});
                            }
                        }
                    }
                } else if (interactionEvent.isSelectionMenuInteraction()) {
                    SelectionMenuInteraction selectionMenuInteraction = interactionEvent.getSelectionMenuEvent();

                    List<SelectOption> selectOptions = interactionEvent.getSelectionMenuEvent().getInteraction().getSelectedOptions();

                    if (selectOptions != null) {
                        for (SelectOption selectedOption : selectOptions) {
                            if (selectedOption.getValue().equals(GENERIC_BUTTON_CLOSE_ID)) {
                                interactionEvent.getButtonClickEvent().getMessage().delete().queue(success -> {}, failure -> {});
                            }
                        }
                    }
                }
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
