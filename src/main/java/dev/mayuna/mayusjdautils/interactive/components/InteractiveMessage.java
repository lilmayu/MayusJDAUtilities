package dev.mayuna.mayusjdautils.interactive.components;

import dev.mayuna.mayusjdautils.exceptions.CannotAddInteractionException;
import dev.mayuna.mayusjdautils.interactive.GroupedInteractionEvent;
import dev.mayuna.mayusjdautils.interactive.Interaction;
import dev.mayuna.mayusjdautils.interactive.InteractionType;
import dev.mayuna.mayusjdautils.interactive.InteractiveListener;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.*;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
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
    private @Getter @Setter MessageEditBuilder messageEditBuilder;
    private @Getter @Setter SelectMenu.Builder<?, ?> selectMenuBuilder;
    private @Getter Pair<Long, TimeUnit> expireAfter = new MutablePair<>(5L, TimeUnit.MINUTES);
    private @Getter Runnable expiredRunnable;
    private @Getter @Setter boolean preventForeignReactions;
    private @Getter @Setter boolean acknowledgeInteractions = true;

    private @Getter Consumer<StringSelectInteractionEvent> stringSelectInteractionEventConsumer = event -> {};
    private @Getter Consumer<EntitySelectInteractionEvent> entitySelectInteractionEventConsumer = event -> {};

    // Discord
    private @Getter Message message;

    //////////////////
    // Constructors //
    //////////////////

    private InteractiveMessage() {
        messageEditBuilder = new MessageEditBuilder();
    }


    private InteractiveMessage(MessageEditBuilder messageEditBuilder) {
        this.messageEditBuilder = messageEditBuilder;
    }

    private InteractiveMessage(MessageEditBuilder messageEditBuilder, SelectMenu.Builder<?, ?> selectMenuBuilder) {
        this.messageEditBuilder = messageEditBuilder;
        this.selectMenuBuilder = selectMenuBuilder;
    }

    /**
     * Creates an empty {@link InteractiveMessage} object ({@link MessageCreateBuilder} is empty)
     *
     * @return {@link InteractiveMessage}
     */
    public static InteractiveMessage createEmpty() {
        return new InteractiveMessage();
    }

    /**
     * Creates {@link InteractiveMessage} object with {@link MessageEditBuilder}
     *
     * @param messageEditBuilder Non-null {@link MessageEditBuilder}
     *
     * @return {@link InteractiveMessage}
     */
    public static InteractiveMessage create(@NonNull MessageEditBuilder messageEditBuilder) {
        return new InteractiveMessage(messageEditBuilder);
    }

    /**
     * Creates {@link InteractiveMessage} object with {@link MessageEditBuilder} and {@link SelectMenu.Builder}
     *
     * @param messageEditBuilder Non-null {@link MessageEditBuilder}
     * @param selectMenuBuilder    Non-null {@link SelectMenu.Builder}
     *
     * @return {@link InteractiveMessage}
     */
    public static InteractiveMessage create(@NonNull MessageEditBuilder messageEditBuilder, @NonNull SelectMenu.Builder selectMenuBuilder) {
        return new InteractiveMessage(messageEditBuilder, selectMenuBuilder);
    }

    /**
     * Creates {@link InteractiveMessage} object with {@link MessageEditBuilder} and specified {@link SelectMenu.Builder}. <strong>The ID of select menu will be randomized.</strong>
     *
     * @param messageEditBuilder  Non-null {@link MessageEditBuilder}
     * @param selectMenuBuilder Non-null {@link SelectMenu.Builder}
     *
     * @return {@link InteractiveMessage}
     */
    public static InteractiveMessage createSelectMenu(@NonNull MessageEditBuilder messageEditBuilder, @NonNull SelectMenu.Builder<?, ?> selectMenuBuilder) {
        selectMenuBuilder.setId(UUID.randomUUID().toString());
        return new InteractiveMessage(messageEditBuilder, selectMenuBuilder);
    }

    /**
     * Creates {@link InteractiveMessage} object with {@link MessageEditBuilder} and randomly created {@link StringSelectMenu.Builder} with specified
     * placeholder
     *
     * @param messageEditBuilder  Non-null {@link MessageEditBuilder}
     * @param selectMenuPlaceholder Non-null Select menu placeholder
     *
     * @return {@link InteractiveMessage}
     */
    public static InteractiveMessage createStringSelectMenu(@NonNull MessageEditBuilder messageEditBuilder, @NonNull String selectMenuPlaceholder) {
        StringSelectMenu.Builder selectMenuBuilder = StringSelectMenu.create(UUID.randomUUID().toString());
        selectMenuBuilder.setPlaceholder(selectMenuPlaceholder);
        return new InteractiveMessage(messageEditBuilder, selectMenuBuilder);
    }

    /**
     * Creates {@link InteractiveMessage} object with {@link MessageEditBuilder} and randomly created {@link EntitySelectMenu.Builder} with specified
     * placeholder
     *
     * @param messageEditBuilder  Non-null {@link MessageEditBuilder}
     * @param selectMenuPlaceholder Non-null Select menu placeholder
     * @param selectTarget  Non-null {@link net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget}
     * @param selectTargets {@link net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget} array
     *
     * @return {@link InteractiveMessage}
     */
    public static InteractiveMessage createEntitySelectMenu(@NonNull MessageEditBuilder messageEditBuilder, @NonNull String selectMenuPlaceholder, @NonNull EntitySelectMenu.SelectTarget selectTarget, EntitySelectMenu.SelectTarget... selectTargets) {
        EntitySelectMenu.Builder selectMenuBuilder;

        if (selectTargets != null) {
            selectMenuBuilder = EntitySelectMenu.create(UUID.randomUUID().toString(), EnumSet.of(selectTarget, selectTargets));
        } else {
            selectMenuBuilder = EntitySelectMenu.create(UUID.randomUUID().toString(), selectTarget);
        }

        selectMenuBuilder.setPlaceholder(selectMenuPlaceholder);
        return new InteractiveMessage(messageEditBuilder, selectMenuBuilder);
    }

    ////////////////
    // Interacted //
    ////////////////

    public InteractiveMessage addInteraction(Interaction interaction, Consumer<GroupedInteractionEvent> onInteracted) {
        Map<Interaction, Consumer<GroupedInteractionEvent>> interactionsButtons = getInteractionByType(InteractionType.BUTTON_CLICK);
        Map<Interaction, Consumer<GroupedInteractionEvent>> interactionsSelectOptions = getInteractionByType(InteractionType.STRING_SELECT_MENU_OPTION_CLICK);

        if (interactionsButtons.size() != 0 && (interaction.getType() == InteractionType.STRING_SELECT_MENU_OPTION_CLICK)) {
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

        if (interaction.getType() == InteractionType.STRING_SELECT_MENU_OPTION_CLICK) {
            if (selectMenuBuilder instanceof EntitySelectMenu.Builder) {
                throw new CannotAddInteractionException("You cannot add interaction to entity select menu! Please, use #onEntitySelectMenuInteracted() method to handle selected values!", interaction);
            }
        }

        interactions.put(interaction, onInteracted);
        return this;
    }

    public InteractiveMessage addInteractionEmpty(Interaction interaction) {
        return addInteraction(interaction, ignored -> {});
    }

    public InteractiveMessage addUserToWhitelist(User user) {
        whitelistedUsers.add(user.getIdLong());
        return this;
    }

    public InteractiveMessage removeUserFromWhitelist(User user) {
        whitelistedUsers.remove(user.getIdLong());
        return this;
    }

    public InteractiveMessage onStringSelectMenuInteracted(@NonNull Consumer<StringSelectInteractionEvent> onInteracted) {
        stringSelectInteractionEventConsumer = onInteracted;
        return this;
    }

    public InteractiveMessage onEntitySelectMenuInteracted(@NonNull Consumer<EntitySelectInteractionEvent> onInteracted) {
        entitySelectInteractionEventConsumer = onInteracted;
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

        MessageCreateAction createMessageAction = null; // Channel#sendMessage() / Channel#sendMessage()
        MessageEditAction editMessageAction = null; // Channel#sendMessage() / Channel#editMessage()
        WebhookMessageCreateAction<Message> hookMessageAction = null; // InteractionHook#sendMessage()
        WebhookMessageEditAction<Message> hookMessageUpdateAction = null; // InteractionHook#editOriginal()

        MessageEditData messageEditData = messageEditBuilder.build();

        if (messageChannelUnion != null) {
            createMessageAction = messageChannelUnion.sendMessage(MessageCreateBuilder.fromEditData(messageEditData).build());
        } else if (interactionHook != null) {
            if (editOriginal) {
                hookMessageUpdateAction = interactionHook.setEphemeral(ephemeral).editOriginal(messageEditData);
            } else {
                hookMessageAction = interactionHook.setEphemeral(ephemeral).sendMessage(MessageCreateBuilder.fromEditData(messageEditData).build());
            }
        } else {
            editMessageAction = messageToEdit.editMessage(messageEditData);
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

                    if (selectMenuBuilder instanceof StringSelectMenu.Builder) {
                        ((StringSelectMenu.Builder)selectMenuBuilder).addOptions(selectOptions);
                    }
                }

                actionRows.add(ActionRow.of(selectMenuBuilder.build()));
            }
        }

        Message sentMessage = null;

        if (createMessageAction != null) {
            createMessageAction = createMessageAction.setComponents(actionRows);
            sentMessage = createMessageAction.complete(); // TODO: Možnost queue?
        } else if (editMessageAction != null) {
            editMessageAction = editMessageAction.setComponents(actionRows);
            sentMessage = editMessageAction.complete(); // TODO: Možnost queue?
        } else {
            if (hookMessageUpdateAction != null) {
                hookMessageUpdateAction = hookMessageUpdateAction.setComponents(actionRows);
                sentMessage = hookMessageUpdateAction.complete(); // TODO: Možnost queue?
            } else {
                hookMessageAction = hookMessageAction.setComponents(actionRows);
                sentMessage = hookMessageAction.complete(); // TODO: Možnost queue?
            }
        }

        if (messageToEdit == null && sentMessage != null) {
            for (Interaction reaction : reactions) {
                sentMessage.addReaction(reaction.getEmoji()).complete(); // TODO: Možnost queue?
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
            case STRING_SELECT_MENU_OPTION_CLICK:
                String selectOptionValueInteraction = interaction.getSelectOption().getValue();
                for (SelectOption selectedOption : event.getStringSelectInteractionEvent().getInteraction().getSelectedOptions()) {
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

        if (event.isEntitySelectMenuInteraction()) {
            if (selectMenuBuilder instanceof EntitySelectMenu.Builder) {
                if (acknowledgeInteractions) {
                    if (!event.getEntitySelectInteractionEvent().isAcknowledged()) {
                        event.getEntitySelectInteractionEvent().deferEdit().queue();
                    }
                }

                entitySelectInteractionEventConsumer.accept(event.getEntitySelectInteractionEvent());

                return;
            }

            // We should not get here. Like, never ever.
        }

        if (selectMenuBuilder instanceof StringSelectMenu.Builder) {
            stringSelectInteractionEventConsumer.accept(event.getStringSelectInteractionEvent());
        }

        for (Map.Entry<Interaction, Consumer<GroupedInteractionEvent>> entry : interactions.entrySet()) {
            Interaction interaction = entry.getKey();

            if (interaction.getType() != event.getInteractionType()) {
                continue;
            }

            if (isApplicable(interaction, event)) {
                if (acknowledgeInteractions) {
                    if (event.isButtonInteraction()) {
                        if (!event.getButtonInteractionEvent().isAcknowledged()) {
                            event.getButtonInteractionEvent().deferEdit().queue();
                        }
                    } else if (event.isStringSelectMenuInteraction()) {
                        if (!event.getStringSelectInteractionEvent().isAcknowledged()) {
                            event.getStringSelectInteractionEvent().deferEdit().queue();
                        }
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
