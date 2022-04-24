package dev.mayuna.mayusjdautils.data;

import dev.mayuna.mayusjdautils.interactive.InteractiveMessage;
import dev.mayuna.mayusjdautils.interactive.InteractiveModal;
import dev.mayuna.mayusjdautils.interactive.evenets.InteractionEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonInteraction;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MayuCoreListener extends ListenerAdapter {

    public static final String GENERIC_BUTTON_CLOSE_ID = "generic_close_button";

    private final static List<InteractiveMessage> interactiveMessageList = Collections.synchronizedList(new LinkedList<>());
    private final static List<InteractiveModal> interactiveModalList = Collections.synchronizedList(new LinkedList<>());

    public static boolean enableExperimentalInteractionBehavior = false;
    public static boolean copyArrayOnInteraction = true;

    public static void addInteractiveMessage(InteractiveMessage intractableMessage) {
        interactiveMessageList.add(intractableMessage);
    }

    public static void removeInteractiveMessage(InteractiveMessage intractableMessage) {
        interactiveMessageList.remove(intractableMessage);
    }

    public static void addInteractiveModal(InteractiveModal interactiveModal) {
        interactiveModalList.add(interactiveModal);
    }

    public static void removeInteractiveModal(InteractiveModal interactiveModal) {
        interactiveModalList.remove(interactiveModal);
    }

    @Override
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

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        event.getInteraction().deferEdit().complete();
        InteractionEvent interactionEvent = new InteractionEvent(event);
        processEvents(interactionEvent);
    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        event.getInteraction().deferEdit().complete();
        InteractionEvent interactionEvent = new InteractionEvent(event);
        processEvents(interactionEvent);
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        InteractionEvent interactionEvent = new InteractionEvent(event);
        processEvents(interactionEvent);
    }

    private void processEvents(InteractionEvent interactionEvent) {

        // Modals
        if (interactionEvent.isModalInteraction()) {
            List<InteractiveModal> interactiveModalListCopy;
            InteractiveModal interactiveModalToRemove = null;

            if (copyArrayOnInteraction) {
                interactiveModalListCopy = new ArrayList<>(interactiveModalList);
            } else {
                interactiveModalListCopy = interactiveModalList;
            }

            for (InteractiveModal interactiveModal : interactiveModalListCopy) {
                String modalId = interactiveModal.getModalBuilder().getId();
                ModalInteractionEvent modalInteractionEvent = interactionEvent.getModalInteractionEvent();

                if (modalId.equals(modalInteractionEvent.getModalId())) {
                    interactiveModalToRemove = interactiveModal;
                    interactiveModal.getOnModalClosed().accept(modalInteractionEvent);
                    break;
                }
            }

            if (interactiveModalToRemove != null) {
                removeInteractiveModal(interactiveModalToRemove);
            }

            return;
        }

        // If it was not modal, it continues to InteractiveMessages

        List<InteractiveMessage> interactiveMessageListCopy;

        if (copyArrayOnInteraction) {
            interactiveMessageListCopy = new ArrayList<>(interactiveMessageList);
        } else {
            interactiveMessageListCopy = interactiveMessageList;
        }

        boolean processed = false;

        for (InteractiveMessage interactiveMessage : interactiveMessageListCopy) {
            if (interactionEvent.isReactionInteraction() && interactiveMessage.isMessage(interactionEvent.getInteractedMessageID())) {
                MessageReactionAddEvent event = interactionEvent.getReactionAddEvent();

                if (event != null && event.getUser() != null) {
                    event.getReaction().removeReaction(event.getUser()).complete();
                }
            }

            boolean success = interactiveMessage.process(interactionEvent);

            if (success) {
                processed = true;
                if (interactiveMessage.isDeleteMessageAfterInteraction()) {
                    interactiveMessage.delete();
                }
                break;
            }
        }

        if (enableExperimentalInteractionBehavior) {
            if (!processed) {
                if (interactionEvent.isButtonInteraction()) {
                    ButtonInteraction buttonInteraction = interactionEvent.getButtonInteractionEvent();
                    Button button = buttonInteraction.getButton();

                    if (button != null) {
                        if (button.getId() != null) {
                            if (button.getId().equals(GENERIC_BUTTON_CLOSE_ID)) {
                                interactionEvent.getButtonInteractionEvent().getMessage().delete().queue(success -> {}, failure -> {});
                            }
                        }
                    }
                } else if (interactionEvent.isSelectMenuInteraction()) {
                    List<SelectOption> selectOptions = interactionEvent.getSelectMenuInteractionEvent().getInteraction().getSelectedOptions();

                    if (selectOptions != null) {
                        for (SelectOption selectedOption : selectOptions) {
                            if (selectedOption.getValue().equals(GENERIC_BUTTON_CLOSE_ID)) {
                                interactionEvent.getButtonInteractionEvent().getMessage().delete().queue(success -> {}, failure -> {});
                            }
                        }
                    }
                }
            }
        }
    }
}
