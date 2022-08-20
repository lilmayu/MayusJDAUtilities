package dev.mayuna.mayusjdautils.interactive.components;

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
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageUpdateAction;
import net.dv8tion.jda.internal.utils.tuple.ImmutablePair;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class InteractiveMessage implements Interactable {

    // Interactions
    private final @Getter Map<Interaction, Consumer<GroupedInteractionEvent>> interactions = new LinkedHashMap<>();

    // Settings
    private final List<Long> whitelistedUsers = new LinkedList<>();
    private @Getter @Setter MessageBuilder messageBuilder;
    private @Getter @Setter SelectMenu.Builder selectMenuBuilder;
    private @Getter Pair<Long, TimeUnit> expireOn;
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
     * @return {@link InteractiveMessage}
     */
    public static InteractiveMessage create(@NonNull MessageBuilder messageBuilder) {
        return new InteractiveMessage(messageBuilder);
    }

    /**
     * Creates {@link InteractiveMessage} object with {@link MessageBuilder} and {@link SelectMenu.Builder}
     *
     * @return {@link InteractiveMessage}
     */
    public static InteractiveMessage create(@NonNull MessageBuilder messageBuilder, @NonNull SelectMenu.Builder selectMenuBuilder) {
        return new InteractiveMessage(messageBuilder, selectMenuBuilder);
    }

    /**
     * Creates {@link InteractiveMessage} object with {@link MessageBuilder} and randomly created {@link SelectMenu.Builder} with specified
     * placeholder
     *
     * @return {@link InteractiveMessage}
     */
    public static InteractiveMessage createSelectMenu(@NonNull MessageBuilder messageBuilder, String selectMenuPlaceholder) {
        SelectMenu.Builder selectMenuBuilder = SelectMenu.create(UUID.randomUUID().toString());
        selectMenuBuilder.setPlaceholder(selectMenuPlaceholder);
        return new InteractiveMessage(messageBuilder, selectMenuBuilder);
    }

    ////////////////
    // Interacted //
    ////////////////

    public InteractiveMessage addInteraction(Interaction interaction, Consumer<GroupedInteractionEvent> onInteracted) {
        // TODO: Checks!

        interactions.put(interaction, onInteracted);
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

    public void expireOn(long number, TimeUnit timeUnit) {
        expireOn = new ImmutablePair<>(number, timeUnit);
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
                return false;
            case REACTION_ADD: case REACTION_REMOVE:
                EmojiUnion emojiReacted = event.getReactionAddEvent().getEmoji();
                EmojiUnion emojiInteraction = interaction.getEmoji();

                return emojiReacted.getAsReactionCode().equals(emojiInteraction.getAsReactionCode());
                break;
            case BUTTON_CLICK:
                Button buttonClicked = event.getButtonInteractionEvent().getButton();
                Button buttonInteraction = interaction.getButton();

                String buttonClickedId = buttonClicked.getId();
                String buttonInteractionId = buttonInteraction.getId();

                if (buttonClickedId == null || buttonInteractionId == null) {
                    return false;
                }

                return buttonClickedId.equals(buttonInteractionId);
            case SELECT_MENU_CLICK:
                break;
            case MODAL_SUBMITTED:
                break;
        }
    }

    ///////////////
    // Overrides //
    ///////////////

    @Override
    public void process(GroupedInteractionEvent interactionEvent) {
        long messageId = interactionEvent.getInteractedMessageId();

        if (message != null) {
            if (message.getIdLong() != messageId) {
                return;
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
        return expireOn;
    }

    @Override
    public void onExpire() {
        if (expiredRunnable != null) {
            expiredRunnable.run();
        }

        Interactable.super.onExpire();
    }
}
