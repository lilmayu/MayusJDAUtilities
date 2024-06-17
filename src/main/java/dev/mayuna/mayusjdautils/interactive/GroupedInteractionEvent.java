package dev.mayuna.mayusjdautils.interactive;

import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ComponentInteraction;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.PremiumRequiredCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

@Getter
public final class GroupedInteractionEvent {

    private ButtonInteractionEvent buttonInteractionEvent = null;
    private StringSelectInteractionEvent stringSelectInteractionEvent = null;
    private EntitySelectInteractionEvent entitySelectInteractionEvent = null;
    private ModalInteractionEvent modalInteractionEvent = null;

    //////////////////
    // Constructors //
    //////////////////

    public GroupedInteractionEvent(ButtonInteractionEvent buttonInteractionEvent) {
        this.buttonInteractionEvent = buttonInteractionEvent;
    }

    public GroupedInteractionEvent(ModalInteractionEvent modalInteractionEvent) {
        this.modalInteractionEvent = modalInteractionEvent;
    }

    public GroupedInteractionEvent(StringSelectInteractionEvent stringSelectInteractionEvent) {
        this.stringSelectInteractionEvent = stringSelectInteractionEvent;
    }

    public GroupedInteractionEvent(EntitySelectInteractionEvent entitySelectInteractionEvent) {
        this.entitySelectInteractionEvent = entitySelectInteractionEvent;
    }

    /**
     * Defers reply to the interaction event<br>
     * Returns null if the interaction type is {@link InteractionType#UNKNOWN}
     *
     * @return Nullable {@link ReplyCallbackAction}
     */
    public ReplyCallbackAction deferReply() {
        switch (getInteractionType()) {
            case BUTTON_CLICK:
                return buttonInteractionEvent.deferReply();
            case STRING_SELECT_MENU_OPTION_CLICK:
                return stringSelectInteractionEvent.deferReply();
            case ENTITY_SELECT_MENU_OPTION_CLICK:
                return entitySelectInteractionEvent.deferReply();
            case MODAL_SUBMITTED:
                return modalInteractionEvent.deferReply();
        }

        return null;
    }

    /**
     * Defers reply to the interaction event<br>
     * Returns null if the interaction type is {@link InteractionType#UNKNOWN}
     *
     * @param ephemeral if true, the reply will be ephemeral
     *
     * @return Nullable {@link ReplyCallbackAction}
     */
    public ReplyCallbackAction deferReply(boolean ephemeral) {
        switch (getInteractionType()) {
            case BUTTON_CLICK:
                return buttonInteractionEvent.deferReply(ephemeral);
            case STRING_SELECT_MENU_OPTION_CLICK:
                return stringSelectInteractionEvent.deferReply(ephemeral);
            case ENTITY_SELECT_MENU_OPTION_CLICK:
                return entitySelectInteractionEvent.deferReply(ephemeral);
            case MODAL_SUBMITTED:
                return modalInteractionEvent.deferReply(ephemeral);
        }

        return null;
    }

    /**
     * Defers edit to the interaction event<br>
     * Returns null if the interaction type is {@link InteractionType#UNKNOWN}
     *
     * @return Nullable {@link MessageEditCallbackAction}
     */
    public MessageEditCallbackAction deferEdit() {
        switch (getInteractionType()) {
            case BUTTON_CLICK:
                return buttonInteractionEvent.deferEdit();
            case STRING_SELECT_MENU_OPTION_CLICK:
                return stringSelectInteractionEvent.deferEdit();
            case ENTITY_SELECT_MENU_OPTION_CLICK:
                return entitySelectInteractionEvent.deferEdit();
            case MODAL_SUBMITTED:
                return modalInteractionEvent.deferEdit();
        }

        return null;
    }

    /**
     * Replies to the interaction event with a modal<br>
     * Returns null if the interaction type is {@link InteractionType#MODAL_SUBMITTED} or {@link InteractionType#UNKNOWN}
     *
     * @param modal Modal to reply with
     *
     * @return Nullable {@link ModalCallbackAction}
     */
    public ModalCallbackAction replyModal(@NonNull Modal modal) {
        ComponentInteraction interaction = getComponentInteraction();

        if (interaction == null) {
            return null;
        }

        return interaction.replyModal(modal);
    }

    /**
     * Replies to the interaction event with a premium required<br>
     * Returns null if the interaction type is {@link InteractionType#MODAL_SUBMITTED} or {@link InteractionType#UNKNOWN}
     *
     * @return Nullable {@link PremiumRequiredCallbackAction}
     */
    public PremiumRequiredCallbackAction replyWithPremiumRequired() {
        ComponentInteraction interaction = getComponentInteraction();

        if (interaction == null) {
            return null;
        }

        return interaction.replyWithPremiumRequired();
    }

    /////////////
    // Getters //
    /////////////

    /**
     * Returns type of this interaction event.<br>
     * This method DOES NOT return JDA's {@link net.dv8tion.jda.api.interactions.InteractionType}!
     *
     * @return Non-null {@link InteractionType}
     */
    public InteractionType getInteractionType() {
        if (isButtonInteraction()) {
            return InteractionType.BUTTON_CLICK;
        }

        if (isStringSelectMenuInteraction()) {
            return InteractionType.STRING_SELECT_MENU_OPTION_CLICK;
        }

        if (isEntitySelectMenuInteraction()) {
            return InteractionType.ENTITY_SELECT_MENU_OPTION_CLICK;
        }

        if (isModalInteraction()) {
            return InteractionType.MODAL_SUBMITTED;
        }

        return InteractionType.UNKNOWN;
    }

    public boolean isButtonInteraction() {
        return buttonInteractionEvent != null;
    }

    public boolean isStringSelectMenuInteraction() {
        return stringSelectInteractionEvent != null;
    }

    public boolean isEntitySelectMenuInteraction() {
        return entitySelectInteractionEvent != null;
    }

    public boolean isModalInteraction() {
        return modalInteractionEvent != null;
    }

    /**
     * Gets {@link InteractionHook} from corresponding event
     *
     * @return Nullable {@link InteractionHook}
     */
    public InteractionHook getInteractionHook() {
        switch (getInteractionType()) {
            case BUTTON_CLICK:
                return buttonInteractionEvent.getHook();
            case STRING_SELECT_MENU_OPTION_CLICK:
                return stringSelectInteractionEvent.getHook();
            case ENTITY_SELECT_MENU_OPTION_CLICK:
                return entitySelectInteractionEvent.getHook();
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
            case BUTTON_CLICK:
                return buttonInteractionEvent.getMessageIdLong();
            case STRING_SELECT_MENU_OPTION_CLICK:
                return stringSelectInteractionEvent.getMessageIdLong();
            case ENTITY_SELECT_MENU_OPTION_CLICK:
                return entitySelectInteractionEvent.getMessageIdLong();
        }

        return 0;
    }

    /**
     * Returns {@link Message} of interacted message.
     *
     * @return {@link Message} of interacted message or if the interaction type is of {@link InteractionType#MODAL_SUBMITTED}, null is returned
     */
    public Message getInteractedMessage() {
        switch (getInteractionType()) {
            case BUTTON_CLICK:
                return buttonInteractionEvent.getMessage();
            case STRING_SELECT_MENU_OPTION_CLICK:
                return stringSelectInteractionEvent.getMessage();
            case ENTITY_SELECT_MENU_OPTION_CLICK:
                return entitySelectInteractionEvent.getMessage();
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
            case BUTTON_CLICK:
                return buttonInteractionEvent.getChannel();
            case STRING_SELECT_MENU_OPTION_CLICK:
                return stringSelectInteractionEvent.getChannel();
            case ENTITY_SELECT_MENU_OPTION_CLICK:
                return entitySelectInteractionEvent.getChannel();
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
            case BUTTON_CLICK:
                return buttonInteractionEvent.getUser();
            case STRING_SELECT_MENU_OPTION_CLICK:
                return stringSelectInteractionEvent.getUser();
            case ENTITY_SELECT_MENU_OPTION_CLICK:
                return entitySelectInteractionEvent.getUser();
            case MODAL_SUBMITTED:
                return modalInteractionEvent.getUser();
        }

        return null;
    }

    /**
     * Returns {@link ComponentInteraction} of interacted message<br>
     * Returns null if {@link InteractionType} is {@link InteractionType#MODAL_SUBMITTED} or {@link InteractionType#UNKNOWN}
     *
     * @return Nullable {@link ComponentInteraction}
     */
    public ComponentInteraction getComponentInteraction() {
        switch (getInteractionType()) {
            case BUTTON_CLICK:
                return buttonInteractionEvent;
            case STRING_SELECT_MENU_OPTION_CLICK:
                return stringSelectInteractionEvent;
            case ENTITY_SELECT_MENU_OPTION_CLICK:
                return entitySelectInteractionEvent;
        }

        return null;
    }
}
