package dev.mayuna.mayusjdautils.interactive;

import dev.mayuna.mayusjdautils.data.MayuCoreListener;
import dev.mayuna.mayusjdautils.exceptions.CannotAddInteractionException;
import dev.mayuna.mayusjdautils.interactive.evenets.InteractionEvent;
import dev.mayuna.mayusjdautils.interactive.objects.Interaction;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class InteractiveMessage {

    // Data
    private final @Getter List<User> whitelistedUsers = new ArrayList<>();
    private final @Getter Map<Interaction, Runnable> interactions = new LinkedHashMap<>();
    private @Getter @Setter MessageBuilder messageBuilder;
    private @Getter @Setter SelectionMenu.Builder selectionMenuBuilder;
    private @Getter @Setter int deleteAfterSeconds = 0;

    // Settings
    private @Getter boolean whitelistUsers = false;
    private @Getter boolean deleteMessageAfterInteraction = true;

    // Discord
    private @Getter Message message;

    // -- Creators -- //

    private InteractiveMessage() {
        messageBuilder = new MessageBuilder();
    }

    private InteractiveMessage(@NonNull MessageBuilder messageBuilder) {
        this.messageBuilder = messageBuilder;
    }

    private InteractiveMessage(@NonNull SelectionMenu.Builder selectionMenuBuilder) {
        messageBuilder = new MessageBuilder();
        this.selectionMenuBuilder = selectionMenuBuilder;
    }

    private InteractiveMessage(@NonNull MessageBuilder messageBuilder, @NonNull SelectionMenu.Builder selectionMenuBuilder) {
        this.messageBuilder = messageBuilder;
        this.selectionMenuBuilder = selectionMenuBuilder;
    }

    public static InteractiveMessage create() {
        return new InteractiveMessage();
    }

    public static InteractiveMessage create(@NonNull MessageBuilder messageBuilder) {
        return new InteractiveMessage(messageBuilder);
    }

    public static InteractiveMessage create(@NonNull SelectionMenu.Builder selectionMenuBuilder) {
        return new InteractiveMessage(selectionMenuBuilder);
    }

    public static InteractiveMessage create(@NonNull MessageBuilder messageBuilder, @NonNull SelectionMenu.Builder selectionMenuBuilder) {
        return new InteractiveMessage(messageBuilder, selectionMenuBuilder);
    }

    public static InteractiveMessage createSelectionMenu() {
        return new InteractiveMessage(SelectionMenu.create(String.valueOf(new Random().nextInt())));
    }

    public static InteractiveMessage createSelectionMenu(@NonNull MessageBuilder messageBuilder) {
        return new InteractiveMessage(messageBuilder, SelectionMenu.create(String.valueOf(new Random().nextInt())));
    }

    // -- Main stuff -- //

    /**
     * Adds Interaction to Intractable Message
     *
     * @param interaction  Interaction object, which is made wit {@link Interaction#asEmoji(String, JDA)} / {@link Interaction#asEmote(Emote)} / {@link Interaction#asButton(Button)} / {@link Interaction#asSelectOption(SelectOption)}
     * @param onInteracted Runnable which will be called when user interacted with specific interaction
     *
     * @return Returns itself - great for chaining
     *
     * @throws CannotAddInteractionException This exception is thrown, if you exceed limit of interactions per message (Reaction - 20, Button/Select Option - 25). Or if you are trying to add Button to Select Menu message and vice-versa.
     */
    public InteractiveMessage addInteraction(Interaction interaction, Runnable onInteracted) throws CannotAddInteractionException {
        Map<Interaction, Runnable> interactionsButtons = getInteractions(InteractionType.BUTTON);
        Map<Interaction, Runnable> interactionsSelectOptions = getInteractions(InteractionType.SELECTION_MENU);

        if (interactionsButtons.size() != 0 && interaction.getInteractionType() == InteractionType.SELECTION_MENU) {
            throw new CannotAddInteractionException("Cannot add Select Option interaction! Message can only have Buttons or Select Menu.", interaction);
        }

        if (interactionsSelectOptions.size() != 0 && interaction.getInteractionType() == InteractionType.BUTTON) {
            throw new CannotAddInteractionException("Cannot add Button interaction! Message can only have Buttons or Select Menu.", interaction);
        }

        if (interactionsButtons.size() == 25) {
            throw new CannotAddInteractionException("Cannot add Button interaction! Maximum number of buttons for message is 25.", interaction);
        }

        if (interactionsSelectOptions.size() == 25) {
            throw new CannotAddInteractionException("Cannot add Select Option interaction! Maximum number of select options for message is 25.", interaction);
        }

        if (getInteractions(InteractionType.REACTION).size() == 20) {
            throw new CannotAddInteractionException("Cannot add Reaction interaction! Maximum number of reactions for message is 20.", interaction);
        }

        interactions.put(interaction, onInteracted);
        return this;
    }

    /**
     * Sends message to specified {@link MessageChannel}. This method can throw standard JDA's exceptions while sending / adding reactions / etc.
     *
     * @param messageChannel Message channel to send message into
     *
     * @return Sent message
     */
    public Message send(@NonNull MessageChannel messageChannel) {
        return sendEx(messageChannel, null, null);
    }

    /**
     * Edits original message in supplied {@link InteractionHook} object. Reactions are not added since Ephemeral messages do not support them.
     *
     * @param interactionHook Non-null InteractionHook object
     *
     * @return Sent ephemeral message
     */
    public Message send(@NonNull InteractionHook interactionHook) {
        return sendEx(null, interactionHook, null);
    }

    public Message edit(@NonNull Message message) {
        return sendEx(null, null, message);
    }

    private Message sendEx(MessageChannel messageChannel, InteractionHook interactionHook, Message editMessage) {
        List<Interaction> reactions = new ArrayList<>();
        List<Button> buttons = new ArrayList<>();
        List<SelectOption> selectOptions = new ArrayList<>();

        for (Map.Entry<Interaction, Runnable> entry : interactions.entrySet()) {
            Interaction interaction = entry.getKey();

            if (interaction.isEmoji() || interaction.isEmote()) {
                reactions.add(interaction);
            } else if (interaction.isButton()) {
                buttons.add(interaction.getButton());
            } else if (interaction.isSelectOption()) {
                selectOptions.add(interaction.getSelectOption());
            }
        }

        MessageAction normalMessageAction = null;
        WebhookMessageUpdateAction<Message> hookMessageAction = null;

        if (messageChannel != null) {
            normalMessageAction = messageChannel.sendMessage(messageBuilder.build());
        } else if (interactionHook != null) {
            hookMessageAction = interactionHook.editOriginal(messageBuilder.build());
        } else if (editMessage != null) {
            normalMessageAction = editMessage.editMessage(messageBuilder.build());
        }

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

            actionRows.add(ActionRow.of(fiveButtons));
        } else {
            if (selectionMenuBuilder != null) {
                selectionMenuBuilder.addOptions(selectOptions);
                actionRows.add(ActionRow.of(selectionMenuBuilder.build()));
            }
        }

        Message message = null;

        if (normalMessageAction != null) {
            normalMessageAction = normalMessageAction.setActionRows(actionRows);
            message = normalMessageAction.complete();

            if (editMessage == null) {
                for (Interaction reaction : reactions) {
                    if (reaction.isEmote()) {
                        message.addReaction(reaction.getEmote()).complete();
                    } else if (reaction.isEmoji()) {
                        message.addReaction(reaction.getEmoji()).complete();
                    }
                }
            }

            if (deleteAfterSeconds != 0) {
                message.delete().queueAfter(deleteAfterSeconds, TimeUnit.SECONDS, success -> {
                    MayuCoreListener.removeIntractableMessage(this);
                }, failure -> {
                    MayuCoreListener.removeIntractableMessage(this);
                });
            }
        } else if (interactionHook != null) {
            hookMessageAction = hookMessageAction.setActionRows(actionRows);
            message = hookMessageAction.complete();
        }

        this.message = message;
        MayuCoreListener.addIntractableMessage(this);

        return message;
    }

    /**
     * Deletes Intractable Message from Discord and internals of Mayu's JDA Utilities
     */
    public void delete() {
        MayuCoreListener.removeIntractableMessage(this);
        if (message != null) {
            if (!message.isEphemeral()) {
                message.delete().queue(success -> {}, failure -> {});
            }
        }
    }

    // -- Setters -- //

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

    // -- Utils -- //

    public Map<Interaction, Runnable> getInteractions(InteractionType interactionType) {
        Map<Interaction, Runnable> interactions = new HashMap<>();

        for (Map.Entry<Interaction, Runnable> entry : this.interactions.entrySet()) {
            if (entry.getKey().getInteractionType() == interactionType) {
                interactions.put(entry.getKey(), entry.getValue());
            }
        }

        return interactions;
    }

    // -- Other -- //

    private boolean isApplicable(Interaction interaction, InteractionEvent interactionEvent) {
        switch (interactionEvent.getInteractionType()) {
            case REACTION:
                if (interaction.getInteractionType() != InteractionType.REACTION) {
                    return false;
                }

                MessageReaction.ReactionEmote reactionEmote = interactionEvent.getReactionAddEvent().getReaction().getReactionEmote();

                if (reactionEmote.isEmote() && interaction.isEmote()) {
                    return reactionEmote.getEmote().getIdLong() == interaction.getEmote().getIdLong();
                } else if (reactionEmote.isEmoji() && interaction.isEmoji()) {
                    return reactionEmote.getEmoji().equals(interaction.getEmoji());
                }

                return false;
            case BUTTON:
                if (interaction.getInteractionType() != InteractionType.BUTTON) {
                    return false;
                }

                Button clickedButton = interactionEvent.getButtonClickEvent().getButton();

                if (clickedButton == null) {
                    return false;
                }

                String clickedButtonID = clickedButton.getId();
                String messageInteractionID = interaction.getButton().getId();

                if (clickedButtonID == null || messageInteractionID == null) {
                    return false;
                }

                return clickedButtonID.equals(messageInteractionID);
            case SELECTION_MENU:
                if (interaction.getInteractionType() != InteractionType.SELECTION_MENU) {
                    return false;
                }

                List<SelectOption> selectOptions = interactionEvent.getSelectionMenuEvent().getInteraction().getSelectedOptions();

                if (selectOptions == null) {
                    return false;
                }

                for (SelectOption selectedOption : selectOptions) {
                    if (selectedOption.getValue().equals(interaction.getSelectOption().getValue())) {
                        return true;
                    }
                }

                return false;
            default:
                return false;
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

        for (Map.Entry<Interaction, Runnable> entry : interactions.entrySet()) {
            Interaction interaction = entry.getKey();

            if (interactionEvent.getInteractionType() == interaction.getInteractionType()) {
                if (isApplicable(interaction, interactionEvent)) {
                    entry.getValue().run();
                    return true;
                }
            }
        }

        return false;
    }

}
