package dev.mayuna.mayusjdautils.interactiveold;

import dev.mayuna.mayusjdautils.data.MayuCoreListener;
import dev.mayuna.mayusjdautils.exceptions.CannotAddInteractionException;
import dev.mayuna.mayusjdautils.interactiveold.objects.Interaction;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class InteractiveMessage {

    // Data
    private final @Getter List<User> whitelistedUsers = new ArrayList<>();
    private final @Getter Map<Interaction, Consumer<InteractionEvent>> interactions = new LinkedHashMap<>();
    private @Getter @Setter MessageBuilder messageBuilder;
    private @Getter @Setter SelectMenu.Builder selectMenuBuilder;
    private @Getter @Setter int deleteAfterSeconds = 0;

    // Settings
    private @Getter boolean whitelistUsers = false;
    private @Getter boolean deleteMessageAfterInteraction = false;

    // Discord
    private @Getter Message message;

    // -- Creators -- //

    private InteractiveMessage() {
        messageBuilder = new MessageBuilder();
    }

    private InteractiveMessage(@NonNull MessageBuilder messageBuilder) {
        this.messageBuilder = messageBuilder;
    }

    private InteractiveMessage(@NonNull SelectMenu.Builder selectMenuBuilder) {
        messageBuilder = new MessageBuilder();
        this.selectMenuBuilder = selectMenuBuilder;
    }

    private InteractiveMessage(@NonNull MessageBuilder messageBuilder, @NonNull SelectMenu.Builder selectMenuBuilder) {
        this.messageBuilder = messageBuilder;
        this.selectMenuBuilder = selectMenuBuilder;
    }

    public static InteractiveMessage create() {
        return new InteractiveMessage();
    }

    public static InteractiveMessage create(@NonNull MessageBuilder messageBuilder) {
        return new InteractiveMessage(messageBuilder);
    }

    public static InteractiveMessage create(@NonNull SelectMenu.Builder selectMenuBuilder) {
        return new InteractiveMessage(selectMenuBuilder);
    }

    public static InteractiveMessage create(@NonNull MessageBuilder messageBuilder, @NonNull SelectMenu.Builder selectMenuBuilder) {
        return new InteractiveMessage(messageBuilder, selectMenuBuilder);
    }

    public static InteractiveMessage createSelectMenu() {
        return new InteractiveMessage(SelectMenu.create(String.valueOf(new Random().nextInt())));
    }

    public static InteractiveMessage createSelectMenu(@NonNull MessageBuilder messageBuilder) {
        return new InteractiveMessage(messageBuilder, SelectMenu.create(String.valueOf(new Random().nextInt())));
    }

    // -- Main stuff -- //

    /**
     * Adds Interaction to Intractable Message
     *
     * @param interaction  Interaction object, which is made wit {@link Interaction#asEmoji(String, JDA)} / {@link Interaction#asEmote(Emote)} / {@link Interaction#asButton(Button)} / {@link Interaction#asSelectOption(SelectOption)}
     * @param onInteracted Consumer (with {@link InteractionEvent}) which will be called when user interacted with specific interaction
     *
     * @return Returns itself - great for chaining
     *
     * @throws CannotAddInteractionException This exception is thrown, if you exceed limit of interactions per message (Reaction - 20, Button/Select Option - 25). Or if you are trying to add Button to Select Menu message and vice-versa.
     */
    public InteractiveMessage addInteraction(Interaction interaction, Consumer<InteractionEvent> onInteracted) throws CannotAddInteractionException {
        Map<Interaction, Consumer<InteractionEvent>> interactionsButtons = getInteractions(InteractionType.BUTTON);
        Map<Interaction, Consumer<InteractionEvent>> interactionsSelectOptions = getInteractions(InteractionType.SELECT_MENU);

        if (interactionsButtons.size() != 0 && interaction.getInteractionType() == InteractionType.SELECT_MENU) {
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
     * Sends message to specified {@link MessageChannel}. This method can throw standard JDA's exceptions while sending / adding reactions / etc.<br>
     * Performs {@link MessageChannel#sendMessage(Message)}
     *
     * @param messageChannel Message channel to send message into
     *
     * @return Sent message
     */
    public Message sendMessage(@NonNull MessageChannel messageChannel) {
        return sendEx(messageChannel, null, false, null);
    }

    /**
     * Edits supplied message with current {@link InteractionHook} object. Reactions might be little glitched.<br>
     * Performs {@link Message#editMessage(Message)}
     *
     * @param message Non-null Message object
     *
     * @return Sent message (should be same as supplied Message)
     */
    public Message editMessage(@NonNull Message message) {
        return sendEx(null, null, false, message);
    }

    /**
     * Edits original message supplied in {@link InteractionHook} object. Reactions are not added since Ephemeral messages do not support them.<br>
     * Performs {@link InteractionHook#editOriginal(Message)}
     *
     * @param interactionHook Non-null InteractionHook object
     *
     * @return Sent ephemeral message
     */
    public Message editOriginal(@NonNull InteractionHook interactionHook) {
        return sendEx(null, interactionHook, true, null);
    }

    /**
     * Sends message supplied to {@link InteractionHook} object. Reactions will be not added since Ephemeral messages do not support them.<br>
     * Performs {@link InteractionHook#sendMessage(Message)}
     *
     * @param interactionHook Non-null InteractionHook object
     *
     * @return Sent ephemeral message
     */
    public Message sendMessage(@NonNull InteractionHook interactionHook) {
        return sendEx(null, interactionHook, false, null);
    }

    /**
     * Sends message supplied to {@link InteractionHook} object. Reactions will be not added since Ephemeral messages do not support them.<br>
     * Performs {@link InteractionHook#sendMessage(Message)}
     *
     * @param interactionHook Non-null InteractionHook object
     * @param ephemeral       Decides if sent interactive message should be ephemeral or not
     *
     * @return Sent ephemeral message
     */
    public Message sendMessage(@NonNull InteractionHook interactionHook, boolean ephemeral) {
        return sendEx(null, interactionHook.setEphemeral(ephemeral), false, null);
    }

    private Message sendEx(MessageChannel messageChannel, InteractionHook interactionHook, boolean editOriginalInteractionHook, Message editMessage) {
        List<Interaction> reactions = new ArrayList<>();
        List<Button> buttons = new ArrayList<>();
        List<SelectOption> selectOptions = new ArrayList<>();

        for (Map.Entry<Interaction, Consumer<InteractionEvent>> entry : interactions.entrySet()) {
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
        WebhookMessageAction<Message> hookMessageAction = null;
        WebhookMessageUpdateAction<Message> hookMessageUpdateAction = null;

        if (messageChannel != null) {
            normalMessageAction = messageChannel.sendMessage(messageBuilder.build());
        } else if (interactionHook != null) {
            if (editOriginalInteractionHook) {
                hookMessageUpdateAction = interactionHook.editOriginal(messageBuilder.build());
            } else {
                hookMessageAction = interactionHook.sendMessage(messageBuilder.build());
            }
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
            if (selectMenuBuilder != null) {
                selectMenuBuilder.addOptions(selectOptions);
                actionRows.add(ActionRow.of(selectMenuBuilder.build()));
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
                    MayuCoreListener.removeInteractiveMessage(this);
                }, failure -> {
                    MayuCoreListener.removeInteractiveMessage(this);
                });
            }
        } else if (interactionHook != null) {
            if (hookMessageUpdateAction != null) {
                hookMessageUpdateAction = hookMessageUpdateAction.setActionRows(actionRows);
                message = hookMessageUpdateAction.complete();
            }

            if (hookMessageAction != null) {
                hookMessageAction = hookMessageAction.addActionRows(actionRows);
                message = hookMessageAction.complete();
            }
        }

        this.message = message;
        MayuCoreListener.addInteractiveMessage(this);

        return message;
    }

    /**
     * Deletes Intractable Message from Discord and internals of Mayu's JDA Utilities
     */
    public void delete() {
        MayuCoreListener.removeInteractiveMessage(this);
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

    public Map<Interaction, Consumer<InteractionEvent>> getInteractions(InteractionType interactionType) {
        Map<Interaction, Consumer<InteractionEvent>> interactions = new HashMap<>();

        for (Map.Entry<Interaction, Consumer<InteractionEvent>> entry : this.interactions.entrySet()) {
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

                Button clickedButton = interactionEvent.getButtonInteractionEvent().getButton();

                if (clickedButton == null) {
                    return false;
                }

                String clickedButtonID = clickedButton.getId();
                String messageInteractionID = interaction.getButton().getId();

                if (clickedButtonID == null || messageInteractionID == null) {
                    return false;
                }

                return clickedButtonID.equals(messageInteractionID);
            case SELECT_MENU:
                if (interaction.getInteractionType() != InteractionType.SELECT_MENU) {
                    return false;
                }

                List<SelectOption> selectOptions = interactionEvent.getSelectMenuInteractionEvent().getInteraction().getSelectedOptions();

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

        User eventUser = interactionEvent.getUser();

        if (eventUser == null) {
            return false;
        }

        if (whitelistUsers) {
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

        for (Map.Entry<Interaction, Consumer<InteractionEvent>> entry : interactions.entrySet()) {
            Interaction interaction = entry.getKey();

            if (interactionEvent.getInteractionType() == interaction.getInteractionType()) {
                if (isApplicable(interaction, interactionEvent)) {
                    if (interactionEvent.isButtonInteraction()) {
                        interactionEvent.getButtonInteractionEvent().deferEdit().queue();
                    } else if (interactionEvent.isSelectMenuInteraction()) {
                        interactionEvent.getSelectMenuInteractionEvent().deferEdit().queue();
                    }

                    entry.getValue().accept(interactionEvent);
                    return true;
                }
            }
        }

        return false;
    }
}
