package dev.mayuna.mayusjdautils.interactive.components;

import dev.mayuna.mayusjdautils.exceptions.CannotAddInteractionException;
import dev.mayuna.mayusjdautils.interactive.GroupedInteractionEvent;
import dev.mayuna.mayusjdautils.interactive.Interaction;
import dev.mayuna.mayusjdautils.interactive.InteractionType;
import dev.mayuna.mayusjdautils.interactive.InteractiveListener;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;
import net.dv8tion.jda.internal.utils.tuple.ImmutablePair;
import net.dv8tion.jda.internal.utils.tuple.MutablePair;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class InteractiveMessage implements Interactable {

    // Interactions
    private final @Getter Map<Interaction, Consumer<GroupedInteractionEvent>> interactions = new LinkedHashMap<>();

    // Settings
    private final List<Long> whitelistedUsers = new LinkedList<>();
    // Other
    private final long createdTime = System.currentTimeMillis();
    private @Getter @Setter MessageBuilder messageBuilder;
    private @Getter @Setter SelectMenu.Builder selectMenuBuilder;
    private @Getter Pair<Long, TimeUnit> expireAfter = new MutablePair<>(5L, TimeUnit.MINUTES);
    private @Getter Runnable expiredRunnable;
    private @Getter @Setter boolean preventForeignReactions;
    // Discord
    private @Getter Message message;

    //////////////////
    // Constructors //
    //////////////////

    private InteractiveMessage() {
        messageBuilder = new MessageBuilder();
    }

    private InteractiveMessage(MessageBuilder messageBuilder) {
        this.messageBuilder = messageBuilder;
    }

    private InteractiveMessage(MessageBuilder messageBuilder, SelectMenu.Builder selectMenuBuilder) {
        this.messageBuilder = messageBuilder;
        this.selectMenuBuilder = selectMenuBuilder;
    }

    /**
     * Creates an empty {@link InteractiveMessage} object ({@link MessageBuilder} is empty)
     *
     * @return {@link InteractiveMessage}
     */
    public static InteractiveMessage createEmpty() {
        return new InteractiveMessage();
    }

    /**
     * Creates {@link InteractiveMessage} object with {@link MessageBuilder}
     *
     * @param messageBuilder Non-null {@link MessageBuilder}
     *
     * @return {@link InteractiveMessage}
     */
    public static InteractiveMessage create(@NonNull MessageBuilder messageBuilder) {
        return new InteractiveMessage(messageBuilder);
    }

    /**
     * Creates {@link InteractiveMessage} object with {@link MessageBuilder} and {@link SelectMenu.Builder}
     *
     * @param messageBuilder Non-null {@link MessageBuilder}
     * @param selectMenuBuilder Non-null {@link SelectMenu.Builder}
     * @return {@link InteractiveMessage}
     */
    public static InteractiveMessage create(@NonNull MessageBuilder messageBuilder, @NonNull SelectMenu.Builder selectMenuBuilder) {
        return new InteractiveMessage(messageBuilder, selectMenuBuilder);
    }

    /**
     * Creates {@link InteractiveMessage} object with {@link MessageBuilder} and randomly created {@link SelectMenu.Builder} with specified
     * placeholder
     *
     * @param messageBuilder        Non-null {@link MessageBuilder}
     * @param selectMenuPlaceholder Non-null Select menu placeholder
     *
     * @return {@link InteractiveMessage}
     */
    public static InteractiveMessage createSelectMenu(@NonNull MessageBuilder messageBuilder, @NonNull String selectMenuPlaceholder) {
        SelectMenu.Builder selectMenuBuilder = SelectMenu.create(UUID.randomUUID().toString());
        selectMenuBuilder.setPlaceholder(selectMenuPlaceholder);
        return new InteractiveMessage(messageBuilder, selectMenuBuilder);
    }

    ////////////////
    // Interacted //
    ////////////////

    public InteractiveMessage addInteraction(Interaction interaction, Consumer<GroupedInteractionEvent> onInteracted) {
        Map<Interaction, Consumer<GroupedInteractionEvent>> interactionsButtons = getInteractionByType(InteractionType.BUTTON_CLICK);
        Map<Interaction, Consumer<GroupedInteractionEvent>> interactionsSelectOptions = getInteractionByType(InteractionType.SELECT_MENU_OPTION_CLICK);

        if (interactionsButtons.size() != 0 && interaction.getType() == InteractionType.SELECT_MENU_OPTION_CLICK) {
            throw new CannotAddInteractionException("Cannot add Select Option interaction! Message can only have Buttons or Select Menu.", interaction);
        }

        if (interactionsSelectOptions.size() != 0 && interaction.getType() == InteractionType.BUTTON_CLICK) {
            throw new CannotAddInteractionException("Cannot add Button interaction! Message can only have Buttons or Select Menu.", interaction);
        }

        if (interactionsButtons.size() == 25) {
            throw new CannotAddInteractionException("Cannot add Button interaction! Maximum number of buttons for message is 25.", interaction);
        }

        if (interactionsSelectOptions.size() == 25) {
            throw new CannotAddInteractionException("Cannot add Select Option interaction! Maximum number of select options for message is 25.", interaction);
        }

        if (getInteractionByType(InteractionType.REACTION_ADD).size() == 20) {
            throw new CannotAddInteractionException("Cannot add Reaction interaction! Maximum number of reactions for message is 20.", interaction);
        }

        interactions.put(interaction, onInteracted);
        return this;
    }

    public InteractiveMessage addUserToWhitelist(User user) {
        whitelistedUsers.add(user.getIdLong());
        return this;
    }

    public InteractiveMessage removeUserFromWhitelist(User user) {
        whitelistedUsers.remove(user.getIdLong());
        return this;
    }

    /////////////////////
    // Sending n Stuff //
    /////////////////////

    public InteractiveMessage sendMessage(@NonNull MessageChannelUnion messageChannelUnion) {
        return sendEx(messageChannelUnion, null, false, false, null);
    }

    public InteractiveMessage editMessage(@NonNull Message message) {
        return sendEx(null, null, false, false, message);
    }

    public InteractiveMessage sendMessage(@NonNull InteractionHook interactionHook) {
        return sendEx(null, interactionHook, false, false, null);
    }

    public InteractiveMessage sendMessage(@NonNull InteractionHook interactionHook, boolean ephemeral) {
        return sendEx(null, interactionHook.setEphemeral(ephemeral), ephemeral, false, null);
    }

    public InteractiveMessage editOriginal(@NonNull InteractionHook interactionHook) {
        return sendEx(null, interactionHook, false, true, null);
    }

    public InteractiveMessage editOriginal(@NonNull InteractionHook interactionHook, boolean ephemeral) {
        return sendEx(null, interactionHook.setEphemeral(ephemeral), ephemeral, true, null);
    }

    private InteractiveMessage sendEx(MessageChannelUnion messageChannelUnion, InteractionHook interactionHook, boolean ephemeral, boolean editOriginal, Message messageToEdit) {
        List<Interaction> reactions = new LinkedList<>();
        List<Button> buttons = new LinkedList<>();
        List<SelectOption> selectOptions = new LinkedList<>();

        interactions.forEach(((interaction, groupedInteractionEventConsumer) -> {
            if (interaction.isEmoji()) {
                reactions.add(interaction);
            } else if (interaction.isButton()) {
                buttons.add(interaction.getButton());
            } else if (interaction.isSelectOption()) {
                selectOptions.add(interaction.getSelectOption());
            }
        }));

        MessageAction normalMessageAction = null; // Channel#sendMessage() / Channel#editMessage()
        WebhookMessageAction<Message> hookMessageAction = null; // InteractionHook#sendMessage()
        WebhookMessageUpdateAction<Message> hookMessageUpdateAction = null; // InteractionHook#editOriginal()

        Message builtMessage = messageBuilder.build();

        if (messageChannelUnion != null) {
            normalMessageAction = messageChannelUnion.sendMessage(builtMessage);
        } else if (interactionHook != null) {
            if (editOriginal) {
                hookMessageUpdateAction = interactionHook.editOriginal(builtMessage);
            } else {
                hookMessageAction = interactionHook.sendMessage(builtMessage);
            }
        } else {
            normalMessageAction = messageToEdit.editMessage(builtMessage);
        }

        List<ActionRow> actionRows = new LinkedList<>();

        if (!buttons.isEmpty()) {
            List<Button> fiveButtons = new ArrayList<>(5);

            for (Button button : buttons) {
                if (fiveButtons.size() == 5) {
                    actionRows.add(ActionRow.of(fiveButtons));

                    fiveButtons = new ArrayList<>(5);
                }

                fiveButtons.add(button);
            }

            actionRows.add(ActionRow.of(fiveButtons));
        } else {
            if (selectMenuBuilder != null) {
                if (!selectOptions.isEmpty()) {
                    selectMenuBuilder.addOptions(selectOptions);
                    actionRows.add(ActionRow.of(selectMenuBuilder.build()));
                }
            }
        }

        Message sentMessage;

        if (normalMessageAction != null) {
            normalMessageAction = normalMessageAction.setActionRows(actionRows);
            sentMessage = normalMessageAction.complete(); // TODO: Možnost queue?

            if (messageToEdit == null) {
                for (Interaction reaction : reactions) {
                    sentMessage.addReaction(reaction.getEmoji()).complete(); // TODO: Možnost queue?
                }
            }
        } else {
            if (hookMessageUpdateAction != null) {
                hookMessageUpdateAction = hookMessageUpdateAction.setActionRows(actionRows);
                sentMessage = hookMessageUpdateAction.complete(); // TODO: Možnost queue?
            } else {
                hookMessageAction = hookMessageAction.addActionRows(actionRows);
                sentMessage = hookMessageAction.complete(); // TODO: Možnost queue?
            }
        }

        this.message = sentMessage;
        InteractiveListener.addInteractable(this);

        return this;
    }

    ////////////
    // Others //
    ////////////

    public void expireAfter(long number, @NonNull TimeUnit timeUnit) {
        expireAfter = new ImmutablePair<>(number, timeUnit);
    }

    public void whenExpired(@NonNull Runnable expiredRunnable) {
        this.expiredRunnable = expiredRunnable;
    }

    /////////////////////
    // Utility methods //
    /////////////////////

    public Map<Interaction, Consumer<GroupedInteractionEvent>> getInteractionByType(InteractionType interactionType) {
        Map<Interaction, Consumer<GroupedInteractionEvent>> interactions = new HashMap<>();

        this.interactions.forEach((interaction, groupedInteractionEventConsumer) -> {
            if (interaction.getType() == interactionType) {
                interactions.put(interaction, groupedInteractionEventConsumer);
            }
        });

        return interactions;
    }

    private boolean isApplicable(Interaction interaction, GroupedInteractionEvent event) {
        if (interaction.getType() != event.getInteractionType()) {
            return false;
        }

        switch (event.getInteractionType()) {
            case UNKNOWN:
            case MODAL_SUBMITTED:
                return false;
            case REACTION_ADD:
                EmojiUnion emojiReacted = event.getReactionAddEvent().getEmoji();
                Emoji emojiInteraction = interaction.getEmoji();

                return emojiReacted.getAsReactionCode().equals(emojiInteraction.getAsReactionCode());
            case BUTTON_CLICK:
                Button buttonClicked = event.getButtonInteractionEvent().getButton();
                Button buttonInteraction = interaction.getButton();

                String buttonClickedId = buttonClicked.getId();
                String buttonInteractionId = buttonInteraction.getId();

                if (buttonClickedId == null || buttonInteractionId == null) {
                    return false;
                }

                return buttonClickedId.equals(buttonInteractionId);
            case SELECT_MENU_OPTION_CLICK:
                String selectOptionValueInteraction = interaction.getSelectOption().getValue();
                for (SelectOption selectedOption : event.getSelectMenuInteractionEvent().getInteraction().getSelectedOptions()) {
                    if (selectOptionValueInteraction.equals(selectedOption.getValue())) {
                        return true;
                    }
                }

                return false;
        }

        return false;
    }

    ///////////////
    // Overrides //
    ///////////////

    @Override
    public void process(GroupedInteractionEvent event) {
        if (event.isModalInteraction()) {
            return;
        }

        long messageId = event.getInteractedMessageId();

        if (message != null) {
            if (message.getIdLong() != messageId) {
                return;
            }
        }

        User user = event.getUser();

        if (user == null || !canInteract(user)) {
            return;
        }

        for (Map.Entry<Interaction, Consumer<GroupedInteractionEvent>> entry : interactions.entrySet()) {
            Interaction interaction = entry.getKey();

            if (interaction.getType() != event.getInteractionType()) {
                return;
            }

            if (isApplicable(interaction, event)) {
                if (event.isButtonInteraction()) {
                    if (!event.getButtonInteractionEvent().isAcknowledged()) {
                        event.getButtonInteractionEvent().deferEdit().queue();
                    }
                } else if (event.isSelectMenuInteraction()) {
                    if (!event.getSelectMenuInteractionEvent().isAcknowledged()) {
                        event.getSelectMenuInteractionEvent().deferEdit().queue();
                    }
                }

                entry.getValue().accept(event);
            }
        }
    }

    @Override
    public boolean canInteract(@NonNull User user) {
        if (whitelistedUsers.isEmpty()) {
            return true;
        }

        return whitelistedUsers.contains(user.getIdLong());
    }

    @Override
    public Pair<Long, TimeUnit> getExpireTime() {
        return expireAfter;
    }

    @Override
    public boolean isExpired() {
        long expireAfterMillis = expireAfter.getRight().toMillis(expireAfter.getLeft());

        if (expireAfterMillis == 0) {
            return false;
        }

        return (createdTime + expireAfterMillis) < System.currentTimeMillis();
    }

    @Override
    public void onExpire() {
        if (expiredRunnable != null) {
            expiredRunnable.run();
        }

        Interactable.super.onExpire();
    }
}
