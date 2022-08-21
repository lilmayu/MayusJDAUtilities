package dev.mayuna.mayusjdautils.interactive;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class GroupedInteractionEvent {

    private @Getter MessageReactionAddEvent reactionAddEvent = null;

    private @Getter ButtonInteractionEvent buttonInteractionEvent = null;
    private @Getter SelectMenuInteractionEvent selectMenuInteractionEvent = null;
    private @Getter ModalInteractionEvent modalInteractionEvent = null;

    //////////////////
    // Constructors //
    //////////////////

    public GroupedInteractionEvent(MessageReactionAddEvent reactionAddEvent) {
        this.reactionAddEvent = reactionAddEvent;
    }

    public GroupedInteractionEvent(ButtonInteractionEvent buttonInteractionEvent) {
        this.buttonInteractionEvent = buttonInteractionEvent;
    }

    public GroupedInteractionEvent(SelectMenuInteractionEvent selectMenuInteractionEvent) {
        this.selectMenuInteractionEvent = selectMenuInteractionEvent;
    }

    public GroupedInteractionEvent(ModalInteractionEvent modalInteractionEvent) {
        this.modalInteractionEvent = modalInteractionEvent;
    }

    /////////////
    // Getters //
    /////////////

    /**
     * Returns type of this interaction event.<br>This method DOES NOT return JDA's {@link net.dv8tion.jda.api.interactions.InteractionType}!
     *
     * @return Non-null {@link InteractionType}
     */
    public InteractionType getInteractionType() {
        if (isReactionInteraction()) {
            return InteractionType.REACTION_ADD;
        }

        if (isButtonInteraction()) {
            return InteractionType.BUTTON_CLICK;
        }

        if (isSelectMenuInteraction()) {
            return InteractionType.SELECT_MENU_OPTION_CLICK;
        }

        if (isModalInteraction()) {
            return InteractionType.MODAL_SUBMITTED;
        }

        return InteractionType.UNKNOWN;
    }

    public boolean isReactionAddInteraction() {
        return reactionAddEvent != null;
    }

    public boolean isReactionRemoveInteraction() {
        return reactionAddEvent != null;
    }

    public boolean isReactionInteraction() {
        return isReactionAddInteraction() || isReactionRemoveInteraction();
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
     * Gets {@link InteractionHook} from {@link ButtonInteractionEvent} or {@link SelectMenuInteractionEvent} (depends on which of these are not
     * null)
     *
     * @return Nullable {@link InteractionHook} (null if {@link GroupedInteractionEvent} is of type {@link InteractionType#REACTION_ADD}
     */
    public InteractionHook getInteractionHook() {
        switch (getInteractionType()) {
            case BUTTON_CLICK:
                return buttonInteractionEvent.getHook();
            case SELECT_MENU_OPTION_CLICK:
                return selectMenuInteractionEvent.getHook();
            case MODAL_SUBMITTED:
                return modalInteractionEvent.getHook();
        }

        return null;
    }

    /**
     * Returns Message ID of interacted message.
     *
     * @return Message ID of interacted message, if the {@link GroupedInteractionEvent} is of type {@link InteractionType#MODAL_SUBMITTED} 0 is
     * returned
     */
    public long getInteractedMessageId() {
        switch (getInteractionType()) {
            case REACTION_ADD:
                return reactionAddEvent.getMessageIdLong();
            case BUTTON_CLICK:
                return buttonInteractionEvent.getMessageIdLong();
            case SELECT_MENU_OPTION_CLICK:
                return selectMenuInteractionEvent.getMessageIdLong();
        }

        return 0;
    }

    /**
     * Returns {@link Message} of interacted message.
     *
     * @return {@link Message} of interacted message, if the {@link GroupedInteractionEvent} is of type {@link InteractionType#REACTION_ADD} (this
     * one only has Message ID - You need it to retrieve it) or {@link InteractionType#MODAL_SUBMITTED} null is returned
     */
    public Message getInteractedMessage() {
        switch (getInteractionType()) {
            case BUTTON_CLICK:
                return buttonInteractionEvent.getMessage();
            case SELECT_MENU_OPTION_CLICK:
                return selectMenuInteractionEvent.getMessage();
        }

        return null;
    }

    /**
     * Returns {@link MessageChannelUnion} of interacted message.
     *
     * @return {@link MessageChannelUnion}, null if {@link InteractionType} is {@link InteractionType#UNKNOWN}
     */
    public MessageChannelUnion getInteractedChannel() {
        switch (getInteractionType()) {
            case REACTION_ADD:
                return reactionAddEvent.getChannel();
            case BUTTON_CLICK:
                return buttonInteractionEvent.getChannel();
            case SELECT_MENU_OPTION_CLICK:
                return selectMenuInteractionEvent.getChannel();
            case MODAL_SUBMITTED:
                return modalInteractionEvent.getChannel();
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
            case REACTION_ADD:
                return reactionAddEvent.getUser();
            case BUTTON_CLICK:
                return buttonInteractionEvent.getUser();
            case SELECT_MENU_OPTION_CLICK:
                return selectMenuInteractionEvent.getUser();
            case MODAL_SUBMITTED:
                return modalInteractionEvent.getUser();
        }

        return null;
    }
}
