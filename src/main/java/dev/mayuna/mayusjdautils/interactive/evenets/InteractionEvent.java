package dev.mayuna.mayusjdautils.interactive.evenets;

import dev.mayuna.mayusjdautils.interactive.InteractionType;
import lombok.Getter;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class InteractionEvent {

    private final @Getter MessageReactionAddEvent reactionAddEvent;
    private final @Getter ButtonInteractionEvent buttonInteractionEvent;
    private final @Getter SelectMenuInteractionEvent selectMenuInteractionEvent;

    public InteractionEvent(MessageReactionAddEvent reactionAddEvent) {
        this.reactionAddEvent = reactionAddEvent;
        this.buttonInteractionEvent = null;
        this.selectMenuInteractionEvent = null;
    }

    public InteractionEvent(ButtonInteractionEvent buttonInteractionEvent) {
        this.reactionAddEvent = null;
        this.buttonInteractionEvent = buttonInteractionEvent;
        this.selectMenuInteractionEvent = null;
    }

    public InteractionEvent(SelectMenuInteractionEvent selectMenuInteractionEvent) {
        this.reactionAddEvent = null;
        this.buttonInteractionEvent = null;
        this.selectMenuInteractionEvent = selectMenuInteractionEvent;
    }

    public InteractionType getInteractionType() {
        if (isReactionInteraction())
            return InteractionType.REACTION;

        if (isButtonInteraction())
            return InteractionType.BUTTON;

        if (isSelectMenuInteraction())
            return InteractionType.SELECT_MENU;

        return null;
    }

    /**
     * Gets {@link InteractionHook} from {@link ButtonInteractionEvent} or {@link SelectMenuInteractionEvent} (depends on which of these are not null)
     * @return Nullable {@link InteractionHook} (null if {@link InteractionEvent} is of type {@link InteractionType#REACTION}
     */
    public InteractionHook getInteractionHook() {
        switch (getInteractionType()) {
            case BUTTON:
                return buttonInteractionEvent.getHook();
            case SELECT_MENU:
                return selectMenuInteractionEvent.getHook();
            default:
                return null;
        }
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

    public long getInteractedMessageID() {
        switch (getInteractionType()) {
            case REACTION:
                if (reactionAddEvent != null) {
                    return reactionAddEvent.getMessageIdLong();
                }
                break;
            case BUTTON:
                if (buttonInteractionEvent != null) {
                    return buttonInteractionEvent.getMessageIdLong();
                }
                break;
            case SELECT_MENU:
                if (selectMenuInteractionEvent != null) {
                    return selectMenuInteractionEvent.getMessageIdLong();
                }
                break;
        }

        return 0;
    }

    public User getUser() {
        switch (getInteractionType()) {
            case REACTION:
                if (reactionAddEvent != null) {
                    return reactionAddEvent.getUser();
                }
                break;
            case BUTTON:
                if (buttonInteractionEvent != null) {
                    return buttonInteractionEvent.getUser();
                }
                break;
            case SELECT_MENU:
                if (selectMenuInteractionEvent != null) {
                    return selectMenuInteractionEvent.getUser();
                }
                break;
        }

        return null;
    }
}
