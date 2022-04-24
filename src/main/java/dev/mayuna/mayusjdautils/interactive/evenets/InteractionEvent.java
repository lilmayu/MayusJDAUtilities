package dev.mayuna.mayusjdautils.interactive.evenets;

import dev.mayuna.mayusjdautils.interactive.InteractionType;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class InteractionEvent {

    private @Getter MessageReactionAddEvent reactionAddEvent = null;
    private @Getter ButtonInteractionEvent buttonInteractionEvent = null;
    private @Getter SelectMenuInteractionEvent selectMenuInteractionEvent = null;
    private @Getter ModalInteractionEvent modalInteractionEvent = null;

    public InteractionEvent(MessageReactionAddEvent reactionAddEvent) {
        this.reactionAddEvent = reactionAddEvent;
    }

    public InteractionEvent(ButtonInteractionEvent buttonInteractionEvent) {
        this.buttonInteractionEvent = buttonInteractionEvent;
    }

    public InteractionEvent(SelectMenuInteractionEvent selectMenuInteractionEvent) {
        this.selectMenuInteractionEvent = selectMenuInteractionEvent;
    }

    public InteractionEvent(ModalInteractionEvent modalInteractionEvent) {
        this.modalInteractionEvent = modalInteractionEvent;
    }

    public InteractionType getInteractionType() {
        if (isReactionInteraction())
            return InteractionType.REACTION;

        if (isButtonInteraction())
            return InteractionType.BUTTON;

        if (isSelectMenuInteraction())
            return InteractionType.SELECT_MENU;

        if (isModalInteraction())
            return InteractionType.MODAL;

        return null;
    }

    /**
     * Gets {@link InteractionHook} from {@link ButtonInteractionEvent} or {@link SelectMenuInteractionEvent} (depends on which of these are not null)
     *
     * @return Nullable {@link InteractionHook} (null if {@link InteractionEvent} is of type {@link InteractionType#REACTION}
     */
    public InteractionHook getInteractionHook() {
        switch (getInteractionType()) {
            case BUTTON:
                return buttonInteractionEvent.getHook();
            case SELECT_MENU:
                return selectMenuInteractionEvent.getHook();
            case MODAL:
                return modalInteractionEvent.getHook();
        }

        return null;
    }

    public boolean isReactionInteraction() {
        return reactionAddEvent != null;
    }

    public boolean isButtonInteraction() {
        return buttonInteractionEvent != null;
    }

    public boolean isSelectMenuInteraction() {
        return selectMenuInteractionEvent != null;
    }

    public boolean isModalInteraction() {
        return modalInteractionEvent != null;
    }

    /**
     * Returns Message ID of interacted message.
     *
     * @return Message ID of interacted message, if the {@link InteractionEvent} is of type {@link InteractionType#MODAL} 0 is returned
     */
    public long getInteractedMessageID() {
        switch (getInteractionType()) {
            case REACTION:
                return reactionAddEvent.getMessageIdLong();
            case BUTTON:
                return buttonInteractionEvent.getMessageIdLong();
            case SELECT_MENU:
                return selectMenuInteractionEvent.getMessageIdLong();
        }

        return 0;
    }

    /**
     * Returns {@link Message} of interacted message.
     *
     * @return {@link Message} of interacted message, if the {@link InteractionEvent} is of type {@link InteractionType#REACTION} or {@link InteractionType#MODAL} null is returned
     */
    public Message getInteractedMessage() {
        switch (getInteractionType()) {
            case BUTTON:
                return buttonInteractionEvent.getMessage();
            case SELECT_MENU:
                return selectMenuInteractionEvent.getMessage();
        }

        return null;
    }

    /**
     * Returns {@link MessageChannel} of interacted message.
     *
     * @return {@link MessageChannel}
     */
    public MessageChannel getInteractedChannel() {
        switch (getInteractionType()) {
            case REACTION:
                return reactionAddEvent.getChannel();
            case BUTTON:
                return buttonInteractionEvent.getChannel();
            case SELECT_MENU:
                return selectMenuInteractionEvent.getChannel();
            case MODAL:
                return modalInteractionEvent.getMessageChannel(); // sus
        }

        return null;
    }

    /**
     * Returns {@link User} of interacted message
     *
     * @return {@link User}
     */
    public User getUser() {
        switch (getInteractionType()) {
            case REACTION:
                return reactionAddEvent.getUser();
            case BUTTON:
                return buttonInteractionEvent.getUser();
            case SELECT_MENU:
                return selectMenuInteractionEvent.getUser();
            case MODAL:
                return modalInteractionEvent.getUser();
        }

        return null;
    }
}
