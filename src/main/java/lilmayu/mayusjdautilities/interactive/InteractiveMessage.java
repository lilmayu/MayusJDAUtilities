package lilmayu.mayusjdautilities.interactive;

import lilmayu.mayusjdautilities.interactive.evenets.InteractionEvent;
import lilmayu.mayusjdautilities.interactive.objects.MessageInteraction;
import lilmayu.mayusjdautilities.data.MayuCoreListener;
import lilmayu.mayusjdautilities.exceptions.CannotAddInteractionException;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.util.*;

public class InteractiveMessage implements IInteractiveMessage {

    private final @Getter List<User> whitelistedUsers = new ArrayList<>();
    private @Getter @Setter MessageBuilder messageBuilder;
    private @Getter Message message;
    private @Getter @Setter SelectionMenu.Builder selectionMenuBuilder;
    private @Getter boolean whitelistUsers = false;
    private @Getter boolean deleteMessageAfterInteraction = true;

    private @Getter Map<MessageInteraction, Runnable> interactions = new LinkedHashMap<>();

    /**
     * Creates Intractable message, you should use this when NOT using Selection Menu (for Selection Menu, see {@link #InteractiveMessage(MessageBuilder, SelectionMenu.Builder)}
     *
     * @param messageBuilder Default message
     */
    public InteractiveMessage(MessageBuilder messageBuilder) {
        this.messageBuilder = messageBuilder;
    }

    /**
     * Creates Intractable Message, this constructor is mainly for Select Menu. If you don't plan using Select Menu, see {@link #InteractiveMessage(MessageBuilder)}
     *
     * @param messageBuilder       Default Message
     * @param selectionMenuBuilder Default Selection Builder -> For placeholder name, etc
     */
    public InteractiveMessage(MessageBuilder messageBuilder, SelectionMenu.Builder selectionMenuBuilder) {
        this.messageBuilder = messageBuilder;
        this.selectionMenuBuilder = selectionMenuBuilder;
    }

    /**
     * Adds Interaction to Intractable Message
     *
     * @param messageInteraction Interaction object, which is made wit {@link MessageInteraction#asEmoji(String, JDA)} / {@link MessageInteraction#asEmote(Emote)} / {@link MessageInteraction#asButton(Button)} / {@link MessageInteraction#asSelectOption(SelectOption)}
     * @param onInteracted      Runnable which will be called when user interacted with specific interaction
     *
     * @return Returns itself - great for chaining
     *
     * @throws CannotAddInteractionException This exception is thrown, if you exceed limit of interactions per message (Reaction -> 20, Button/Select Option -> 25). Or if you are trying to add Button to Select Menu message and vice-versa.
     */
    public InteractiveMessage addInteraction(MessageInteraction messageInteraction, Runnable onInteracted) throws CannotAddInteractionException {
        Map<MessageInteraction, Runnable> interactionsButtons = getInteractions(InteractionType.BUTTON);
        Map<MessageInteraction, Runnable> interactionsSelectOptions = getInteractions(InteractionType.SELECTION_MENU);

        if (interactionsButtons.size() != 0 && messageInteraction.getInteractionType() == InteractionType.SELECTION_MENU) {
            throw new CannotAddInteractionException("Cannot add Select Option interaction! Message can only have Buttons or Select Menu.", messageInteraction);
        }

        if (interactionsSelectOptions.size() != 0 && messageInteraction.getInteractionType() == InteractionType.BUTTON) {
            throw new CannotAddInteractionException("Cannot add Button interaction! Message can only have Buttons or Select Menu.", messageInteraction);
        }

        if (interactionsButtons.size() == 25) {
            throw new CannotAddInteractionException("Cannot add Button interaction! Maximum number of buttons for message is 25.", messageInteraction);
        }

        if (interactionsSelectOptions.size() == 25) {
            throw new CannotAddInteractionException("Cannot add Select Option interaction! Maximum number of select options for message is 25.", messageInteraction);
        }

        if (getInteractions(InteractionType.REACTION).size() == 20) {
            throw new CannotAddInteractionException("Cannot add Reaction interaction! Maximum number of reactions for message is 20.", messageInteraction);
        }

        interactions.put(messageInteraction, onInteracted);
        return this;
    }

    /**
     * Sets flag for Intractable Message, if it should ignore non-whitelisted users in using interactions
     *
     * @param whitelistUsers Boolean flag, if it should care about whitelist of users; Default value is false
     *
     * @return Returns itself - great for chaining
     */
    public InteractiveMessage setWhitelistUsers(boolean whitelistUsers) {
        this.whitelistUsers = whitelistUsers;
        return this;
    }

    public InteractiveMessage addWhitelistUser(User user) {
        this.whitelistedUsers.add(user);
        return this;
    }

    public InteractiveMessage setDeleteMessageAfterInteraction(boolean deleteMessageAfterInteraction) {
        this.deleteMessageAfterInteraction = deleteMessageAfterInteraction;
        return this;
    }

    public Map<MessageInteraction, Runnable> getInteractions(InteractionType interactionType) {
        Map<MessageInteraction, Runnable> interactions = new HashMap<>();

        for (Map.Entry<MessageInteraction, Runnable> entry : this.interactions.entrySet()) {
            if (entry.getKey().getInteractionType() == interactionType) {
                interactions.put(entry.getKey(), entry.getValue());
            }
        }

        return interactions;
    }

    /**
     * Sends message to specified {@link MessageChannel}. This method can throw standard JDA's exceptions while sending / adding reactions / etc.
     *
     * @param messageChannel Message channel to send message into
     *
     * @return Sent message
     */
    public Message sendMessage(MessageChannel messageChannel) {
        List<MessageInteraction> reactions = new ArrayList<>();
        List<Button> buttons = new ArrayList<>();
        List<SelectOption> selectOptions = new ArrayList<>();

        for (Map.Entry<MessageInteraction, Runnable> entry : interactions.entrySet()) {
            MessageInteraction messageInteraction = entry.getKey();

            if (messageInteraction.isEmoji() || messageInteraction.isEmote()) {
                reactions.add(messageInteraction);
            } else if (messageInteraction.isButton()) {
                buttons.add(messageInteraction.getButton());
            } else if (messageInteraction.isSelectOption()) {
                selectOptions.add(messageInteraction.getSelectOption());
            }
        }

        MessageAction messageAction = messageChannel.sendMessage(messageBuilder.build());

        // Buttons / SelectMenu
        List<ActionRow> actionRows = new ArrayList<>();

        if (buttons.size() != 0) {
            List<Button> fiveButtons = new ArrayList<>();

            for (Button button : buttons) {
                if (fiveButtons.size() == 5) {
                    actionRows.add(ActionRow.of(fiveButtons));

                    fiveButtons = new ArrayList<>();
                }

                fiveButtons.add(button);
            }

            if (fiveButtons.size() != 0) {
                actionRows.add(ActionRow.of(fiveButtons));
            }
        } else {
            if (selectionMenuBuilder != null) {
                selectionMenuBuilder.addOptions(selectOptions);
                actionRows.add(ActionRow.of(selectionMenuBuilder.build()));
            }
        }

        messageAction = messageAction.setActionRows(actionRows);

        Message message = messageAction.complete();

        for (MessageInteraction reaction : reactions) {
            if (reaction.isEmote()) {
                message.addReaction(reaction.getEmote()).complete();
            } else if (reaction.isEmoji()) {
                message.addReaction(reaction.getEmoji()).complete();
            }
        }

        this.message = message;
        MayuCoreListener.addIntractableMessage(this);
        return message;
    }

    private boolean isApplicable(MessageInteraction messageInteraction, InteractionEvent interactionEvent) {
        switch (interactionEvent.getInteractionType()) {
            case REACTION:
                if (messageInteraction.getInteractionType() != InteractionType.REACTION) {
                    return false;
                }

                MessageReaction.ReactionEmote reactionEmote = interactionEvent.getReactionAddEvent().getReaction().getReactionEmote();

                if (reactionEmote.isEmote() && messageInteraction.isEmote()) {
                    if (reactionEmote.getEmote().getIdLong() == messageInteraction.getEmote().getIdLong()) {
                        return true;
                    }
                } else if (reactionEmote.isEmoji() && messageInteraction.isEmoji()) {
                    if (reactionEmote.getEmoji().equals(messageInteraction.getEmoji())) {
                        return true;
                    }
                }

                return false;
            case BUTTON:
                if (messageInteraction.getInteractionType() != InteractionType.BUTTON) {
                    return false;
                }

                Button clickedButton = interactionEvent.getButtonClickEvent().getButton();

                if (clickedButton == null) {
                    return false;
                }

                String clickedButtonID = clickedButton.getId();
                String messageInteractionID = messageInteraction.getButton().getId();

                if (clickedButtonID == null || messageInteractionID == null) {
                    return false;
                }

                return clickedButtonID.equals(messageInteractionID);
            case SELECTION_MENU:
                if (messageInteraction.getInteractionType() != InteractionType.SELECTION_MENU) {
                    return false;
                }

                List<SelectOption> selectOptions = interactionEvent.getSelectionMenuEvent().getInteraction().getSelectedOptions();

                if (selectOptions == null) {
                    return false;
                }

                for (SelectOption selectedOption : selectOptions) {
                    if (selectedOption.getValue().equals(messageInteraction.getSelectOption().getValue())) {
                        return true;
                    }
                }

                return false;
            default:
                return false;
        }
    }

    /**
     * Deletes Intractable Message from Discord and internals of Mayu's JDA Utilities
     */
    public void delete() {
        MayuCoreListener.removeIntractableMessage(this);
        if (message != null) {
            message.delete().complete();
        }
    }

    /**
     * Checks if supplied message ID is same as Intractable Message's ID (if it was sent)
     *
     * @param messageID Message ID
     *
     * @return True / False, depending if message was sent + if IDs match
     */
    public boolean isMessage(long messageID) {
        return this.message != null && this.message.getIdLong() == messageID;
    }

    /**
     * You don't need to call this method. This method is used by event listener in Mayu's JDA Utilities and it is called when any interaction happened.
     *
     * @param interactionEvent InteractionEvent object
     *
     * @return True / False
     */
    @Override
    public boolean process(InteractionEvent interactionEvent) {
        if (this.message.getIdLong() != interactionEvent.getInteractedMessageID()) {
            return false;
        }

        if (whitelistUsers) {
            User eventUser = interactionEvent.getUser();

            if (eventUser == null) {
                return false;
            }

            boolean allow = false;
            for (User whitelistUser : whitelistedUsers) {
                if (whitelistUser.getIdLong() == eventUser.getIdLong()) {
                    allow = true;
                }
            }

            if (!allow) {
                return false;
            }
        }

        for (Map.Entry<MessageInteraction, Runnable> entry : interactions.entrySet()) {
            MessageInteraction messageInteraction = entry.getKey();

            if (interactionEvent.getInteractionType() == messageInteraction.getInteractionType()) {
                if (isApplicable(messageInteraction, interactionEvent)) {
                    entry.getValue().run();
                    return true;
                }
            }
        }

        return false;
    }
}
