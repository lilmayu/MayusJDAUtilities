package dev.mayuna.mayusjdautils.interactive.evenets;

import dev.mayuna.mayusjdautils.interactive.InteractionType;
import lombok.Getter;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class InteractionEvent {

    private final @Getter MessageReactionAddEvent reactionAddEvent;
    private final @Getter ButtonClickEvent buttonClickEvent;
    private final @Getter SelectionMenuEvent selectionMenuEvent;

    public InteractionEvent(MessageReactionAddEvent reactionAddEvent) {
        this.reactionAddEvent = reactionAddEvent;
        this.buttonClickEvent = null;
        this.selectionMenuEvent = null;
    }

    public InteractionEvent(ButtonClickEvent buttonClickEvent) {
        this.reactionAddEvent = null;
        this.buttonClickEvent = buttonClickEvent;
        this.selectionMenuEvent = null;
    }

    public InteractionEvent(SelectionMenuEvent selectionMenuEvent) {
        this.reactionAddEvent = null;
        this.buttonClickEvent = null;
        this.selectionMenuEvent = selectionMenuEvent;
    }

    public InteractionType getInteractionType() {
        if (isReactionInteraction())
            return InteractionType.REACTION;

        if (isButtonInteraction())
            return InteractionType.BUTTON;

        if (isSelectionMenuInteraction())
            return InteractionType.SELECTION_MENU;

        return null;
    }

    public boolean isReactionInteraction() {
        return reactionAddEvent != null;
    }

    public boolean isButtonInteraction() {
        return buttonClickEvent != null;
    }

    public boolean isSelectionMenuInteraction() {
        return selectionMenuEvent != null;
    }

    public long getInteractedMessageID() {
        switch (getInteractionType()) {
            case REACTION:
                if (reactionAddEvent != null) {
                    return reactionAddEvent.getMessageIdLong();
                }
                break;
            case BUTTON:
                if (buttonClickEvent != null) {
                    return buttonClickEvent.getMessageIdLong();
                }
                break;
            case SELECTION_MENU:
                if (selectionMenuEvent != null) {
                    return selectionMenuEvent.getMessageIdLong();
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
                if (buttonClickEvent != null) {
                    return buttonClickEvent.getUser();
                }
                break;
            case SELECTION_MENU:
                if (selectionMenuEvent != null) {
                    return selectionMenuEvent.getUser();
                }
                break;
        }

        return null;
    }
}
