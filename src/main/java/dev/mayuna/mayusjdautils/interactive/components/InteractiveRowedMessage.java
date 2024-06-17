package dev.mayuna.mayusjdautils.interactive.components;

import dev.mayuna.mayusjdautils.exceptions.CannotAddInteractionException;
import dev.mayuna.mayusjdautils.interactive.GroupedInteractionEvent;
import dev.mayuna.mayusjdautils.interactive.Interaction;
import dev.mayuna.mayusjdautils.interactive.InteractiveListener;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.requests.RestAction;
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

/**
 * Interactive message that supports multiple action rows and interactions with different components
 */
public final class InteractiveRowedMessage implements Interactable {

    private static final String randomFillerValue = UUID.randomUUID().toString();

    private final @Getter List<ActionRow> actionRows = new ArrayList<>(Message.MAX_COMPONENT_COUNT);

    private final @Getter Map<Interaction, Consumer<GroupedInteractionEvent>> interactions = new LinkedHashMap<>();
    private final @Getter Map<String, Consumer<StringSelectInteractionEvent>> stringSelectInteractionEventConsumerMap = new LinkedHashMap<>();
    private final @Getter Map<String, Consumer<EntitySelectInteractionEvent>> entitySelectInteractionEventConsumerMap = new LinkedHashMap<>();

    // Settings
    private final List<Long> whitelistedUsers = new LinkedList<>();

    // Other
    private final long createdTime = System.currentTimeMillis();
    private @Getter @Setter MessageEditBuilder messageEditBuilder;
    private @Getter Pair<Long, TimeUnit> expireAfter = new MutablePair<>(5L, TimeUnit.MINUTES);
    private @Getter Runnable expiredRunnable;

    private InteractiveRowedMessage() {
        for (int i = 0; i < Message.MAX_COMPONENT_COUNT; i++) {
            actionRows.add(ActionRow.of(TextInput.create("aw", "man", TextInputStyle.SHORT).build()));
        }

        // Removing all components from action rows for action rows to be empty
        for (int i = 0; i < Message.MAX_COMPONENT_COUNT; i++) {
            actionRows.get(i).getComponents().clear();
        }
    }

    /**
     * Creates new instance of {@link Builder}
     *
     * @return {@link Builder}
     */
    public static Builder builder(MessageEditBuilder builder) {
        return new Builder(builder);
    }

    @Override
    public void process(GroupedInteractionEvent event) {
        if (event.isModalInteraction()) {
            return;
        }

        User user = event.getUser();

        if (user == null || !canInteract(user)) {
            return;
        }

        if (event.isEntitySelectMenuInteraction()) {
            String componentId = event.getEntitySelectInteractionEvent().getComponentId();

            Consumer<EntitySelectInteractionEvent> consumer = entitySelectInteractionEventConsumerMap.get(componentId);

            if (consumer != null) {
                consumer.accept(event.getEntitySelectInteractionEvent());
                return;
            }
        }

        if (event.isStringSelectMenuInteraction()) {
            String componentId = event.getStringSelectInteractionEvent().getComponentId();

            Consumer<StringSelectInteractionEvent> consumer = stringSelectInteractionEventConsumerMap.get(componentId);

            if (consumer != null) {
                consumer.accept(event.getStringSelectInteractionEvent());
                return;
            }
        }

        for (Map.Entry<Interaction, Consumer<GroupedInteractionEvent>> entry : interactions.entrySet()) {
            Interaction interaction = entry.getKey();

            if (interaction.getType() != event.getInteractionType()) {
                continue;
            }

            if (isApplicable(interaction, event)) {
                entry.getValue().accept(event);
            }
        }
    }

    private boolean isApplicable(Interaction interaction, GroupedInteractionEvent event) {
        if (interaction.getType() != event.getInteractionType()) {
            return false;
        }

        switch (event.getInteractionType()) {
            case UNKNOWN:
            case MODAL_SUBMITTED:
                return false;
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

    public InteractiveRowedMessage addUserToWhitelist(User user) {
        whitelistedUsers.add(user.getIdLong());
        return this;
    }

    public InteractiveRowedMessage removeUserFromWhitelist(User user) {
        whitelistedUsers.remove(user.getIdLong());
        return this;
    }

    @Override
    public boolean canInteract(@NonNull User user) {
        if (whitelistedUsers.isEmpty()) {
            return true;
        }

        return whitelistedUsers.contains(user.getIdLong());
    }

    public InteractiveRowedMessage expireAfter(long number, @NonNull TimeUnit timeUnit) {
        expireAfter = new ImmutablePair<>(number, timeUnit);
        return this;
    }

    public InteractiveRowedMessage whenExpired(@NonNull Runnable expiredRunnable) {
        this.expiredRunnable = expiredRunnable;
        return this;
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

    private ActionRow getActionRowByIndex(int index) {
        if (index < 0 || index > Message.MAX_COMPONENT_COUNT) {
            throw new IndexOutOfBoundsException("Invalid action row index " + index + " - bounds are 0 to " + Message.MAX_COMPONENT_COUNT);
        }

        return actionRows.get(index);
    }


    /////////////////////
    // Sending n Stuff //
    /////////////////////

    /**
     * Sends the interactive message to the specified {@link MessageChannelUnion}
     *
     * @param messageChannelUnion Non-null {@link MessageChannelUnion}
     *
     * @return {@link RestAction} of {@link Message}
     */
    public RestAction<Message> sendMessage(@NonNull MessageChannelUnion messageChannelUnion) {
        return sendEx(messageChannelUnion, null, false, false, null, null);
    }

    /**
     * Edits specified {@link Message} with the interactive message
     *
     * @param message Non-null {@link Message}
     *
     * @return {@link RestAction} of {@link Message}
     */
    public RestAction<Message> editMessage(@NonNull Message message) {
        return sendEx(null, null, false, false, message, null);
    }

    /**
     * Replies to the specified {@link Message}
     *
     * @param message Non-null {@link Message}
     *
     * @return {@link RestAction} of {@link Message}
     */
    public RestAction<Message> replyTo(@NonNull Message message) {
        return sendEx(null, null, false, false, null, message);
    }

    /**
     * Sends the interactive message to the specified {@link InteractionHook}
     *
     * @param interactionHook Non-null {@link InteractionHook}
     *
     * @return {@link RestAction} of {@link Message}
     */
    public RestAction<Message> sendMessage(@NonNull InteractionHook interactionHook) {
        return sendEx(null, interactionHook, false, false, null, null);
    }

    /**
     * Sends the interactive message to the specified {@link InteractionHook} with specified ephemeral
     *
     * @param interactionHook Non-null {@link InteractionHook}
     * @param ephemeral       Ephemeral
     *
     * @return {@link RestAction} of {@link Message}
     */
    public RestAction<Message> sendMessage(@NonNull InteractionHook interactionHook, boolean ephemeral) {
        return sendEx(null, interactionHook.setEphemeral(ephemeral), ephemeral, false, null, null);
    }

    /**
     * Edits the original message of the specified {@link InteractionHook}
     *
     * @param interactionHook Non-null {@link InteractionHook}
     *
     * @return {@link RestAction} of {@link Message}
     */
    public RestAction<Message> editOriginal(@NonNull InteractionHook interactionHook) {
        return sendEx(null, interactionHook, false, true, null, null);
    }

    /**
     * Edits the original message of the specified {@link InteractionHook} with specified ephemeral
     *
     * @param interactionHook Non-null {@link InteractionHook}
     * @param ephemeral       Ephemeral
     *
     * @return {@link RestAction} of {@link Message}
     */
    public RestAction<Message> editOriginal(@NonNull InteractionHook interactionHook, boolean ephemeral) {
        return sendEx(null, interactionHook.setEphemeral(ephemeral), ephemeral, true, null, null);
    }

    private RestAction<Message> sendEx(MessageChannelUnion messageChannelUnion, InteractionHook interactionHook, boolean ephemeral, boolean editOriginal, Message messageToEdit, Message messageToReplyTo) {
        List<Button> buttons = new LinkedList<>();
        List<SelectOption> selectOptions = new LinkedList<>();

        interactions.forEach(((interaction, groupedInteractionEventConsumer) -> {
            if (interaction.isButton()) {
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
        } else if (messageToEdit != null) {
            editMessageAction = messageToEdit.editMessage(messageEditData);
        } else {
            createMessageAction = messageToReplyTo.reply(MessageCreateBuilder.fromEditData(messageEditData).build());
        }

        List<ActionRow> actionRowsCopy = new ArrayList<>(5);

        for (int i = 0; i < actionRows.size(); i++) {
            ActionRow actionRow = getActionRowByIndex(i);

            if (!actionRow.getComponents().isEmpty()) {
                actionRowsCopy.add(actionRow);
            }
        }

        RestAction<Message> restAction;

        if (createMessageAction != null) {
            restAction = createMessageAction.setComponents(actionRowsCopy);
        } else if (editMessageAction != null) {
            restAction = editMessageAction.setComponents(actionRowsCopy);
        } else {
            if (hookMessageUpdateAction != null) {
                restAction = hookMessageUpdateAction.setComponents(actionRowsCopy);
            } else {
                restAction = hookMessageAction.setComponents(actionRowsCopy);
            }
        }

        InteractiveListener.addInteractable(this);

        return restAction;
    }

    public static class Builder {

        private final InteractiveRowedMessage interactiveRowedMessage = new InteractiveRowedMessage();

        /**
         * Creates new instance of {@link Builder}
         *
         * @param builder {@link MessageEditBuilder}
         */
        public Builder(MessageEditBuilder builder) {
            interactiveRowedMessage.setMessageEditBuilder(builder);
        }

        /**
         * Adds a SelectMenu to the specified action row index
         *
         * @param actionRowIndex Index of the action row
         * @param selectMenu     SelectMenu to add
         *
         * @return Builder
         */
        protected Builder addSelectMenu(int actionRowIndex, SelectMenu.Builder<?, ?> selectMenu) {
            ActionRow actionRow = interactiveRowedMessage.getActionRowByIndex(actionRowIndex);

            if (!actionRow.getComponents().isEmpty()) {
                throw new IllegalArgumentException("On action row index " + actionRowIndex + " are already some components. Cannot add Select Menu!");
            }

            selectMenu.setId(UUID.randomUUID().toString());

            actionRow.getComponents().add(selectMenu.build());
            return this;
        }

        /**
         * Adds string SelectMenu to the specified action row index
         *
         * @param actionRowIndex        Index of the action row
         * @param selectMenuPlaceholder Placeholder for the SelectMenu
         * @param buildSelectMenu       Consumer for building the SelectMenu
         * @param onInteracted          Consumer for handling the interaction
         *
         * @return Builder
         */
        public Builder addStringSelectMenu(int actionRowIndex, String selectMenuPlaceholder, Consumer<StringSelectMenu.Builder> buildSelectMenu, Consumer<StringSelectInteractionEvent> onInteracted) {
            StringSelectMenu.Builder selectMenuBuilder = StringSelectMenu.create(UUID.randomUUID().toString());
            selectMenuBuilder.setPlaceholder(selectMenuPlaceholder);

            if (buildSelectMenu != null) {
                buildSelectMenu.accept(selectMenuBuilder);
            }

            addSelectMenu(actionRowIndex, selectMenuBuilder);

            if (onInteracted != null) {
                onStringSelectMenuInteracted(actionRowIndex, onInteracted);
            }

            return this;
        }

        /**
         * Adds string SelectMenu to the specified action row index
         *
         * @param actionRowIndex        Index of the action row
         * @param selectMenuPlaceholder Placeholder for the SelectMenu
         * @param buildSelectMenu       Consumer for building the SelectMenu
         *
         * @return Builder
         */
        public Builder addStringSelectMenu(int actionRowIndex, String selectMenuPlaceholder, Consumer<StringSelectMenu.Builder> buildSelectMenu) {
            addStringSelectMenu(actionRowIndex, selectMenuPlaceholder, buildSelectMenu, null);
            return this;
        }

        /**
         * Adds string SelectMenu to the specified action row index
         *
         * @param actionRowIndex        Index of the action row
         * @param selectMenuPlaceholder Placeholder for the SelectMenu
         *
         * @return Builder
         */
        public Builder addStringSelectMenu(int actionRowIndex, String selectMenuPlaceholder) {
            addStringSelectMenu(actionRowIndex, selectMenuPlaceholder, null, null);
            return this;
        }

        /**
         * Adds entity SelectMenu to the specified action row index
         *
         * @param actionRowIndex        Index of the action row
         * @param selectMenuPlaceholder Placeholder for the SelectMenu
         * @param selectTargets         SelectTargets
         * @param onInteracted          Consumer for handling the interaction
         * @param buildSelectMenu       Consumer for building the SelectMenu
         *
         * @return Builder
         */
        public Builder addEntitySelectMenu(int actionRowIndex, String selectMenuPlaceholder, Collection<EntitySelectMenu.SelectTarget> selectTargets, Consumer<EntitySelectMenu.Builder> buildSelectMenu, Consumer<EntitySelectInteractionEvent> onInteracted) {
            EntitySelectMenu.Builder selectMenuBuilder = EntitySelectMenu.create(UUID.randomUUID().toString(), selectTargets);
            selectMenuBuilder.setPlaceholder(selectMenuPlaceholder);
            buildSelectMenu.accept(selectMenuBuilder);
            addSelectMenu(actionRowIndex, selectMenuBuilder);

            if (onInteracted != null) {
                onEntitySelectMenuInteracted(actionRowIndex, onInteracted);
            }

            return this;
        }

        /**
         * Adds entity SelectMenu to the specified action row index
         *
         * @param actionRowIndex        Index of the action row
         * @param selectMenuPlaceholder Placeholder for the SelectMenu
         * @param selectTargets         SelectTargets
         * @param buildSelectMenu       Consumer for building the SelectMenu
         *
         * @return Builder
         */
        public Builder addEntitySelectMenu(int actionRowIndex, String selectMenuPlaceholder, List<EntitySelectMenu.SelectTarget> selectTargets, Consumer<EntitySelectMenu.Builder> buildSelectMenu) {
            addEntitySelectMenu(actionRowIndex, selectMenuPlaceholder, selectTargets, buildSelectMenu, null);
            return this;
        }

        /**
         * Handles interaction with String Select Menu
         *
         * @param actionRowIndex Index of the action row
         * @param onInteracted   Consumer for handling the interaction
         *
         * @return Builder
         */
        public Builder onStringSelectMenuInteracted(int actionRowIndex, @NonNull Consumer<StringSelectInteractionEvent> onInteracted) {
            ActionRow actionRow = interactiveRowedMessage.getActionRowByIndex(actionRowIndex);

            if (actionRow.getComponents().size() != 1 || actionRow.getComponents().get(0).getType() != Component.Type.STRING_SELECT) {
                throw new IllegalArgumentException("On action row index " + actionRowIndex + " there are other components. Cannot add String Select Menu Interaction!");
            }

            String componentId = ((StringSelectMenu) actionRow.getComponents().get(0)).getId();
            interactiveRowedMessage.stringSelectInteractionEventConsumerMap.put(componentId, onInteracted);
            return this;
        }

        /**
         * Handles interaction with Entity Select Menu
         *
         * @param actionRowIndex Index of the action row
         * @param onInteracted   Consumer for handling the interaction
         *
         * @return Builder
         */
        public Builder onEntitySelectMenuInteracted(int actionRowIndex, @NonNull Consumer<EntitySelectInteractionEvent> onInteracted) {
            ActionRow actionRow = interactiveRowedMessage.getActionRowByIndex(actionRowIndex);

            if (actionRow.getComponents().size() != 1 || !(actionRow.getComponents().get(0) instanceof EntitySelectMenu)) {
                throw new IllegalArgumentException("On action row index " + actionRowIndex + " there are other components. Cannot add Entity Select Menu Interaction!");
            }

            String componentId = ((EntitySelectMenu) actionRow.getComponents().get(0)).getId();
            interactiveRowedMessage.entitySelectInteractionEventConsumerMap.put(componentId, onInteracted);
            return this;
        }

        /**
         * Appends interaction to the specified action row index, e.g., button or select option
         *
         * @param actionRowIndex Index of the action row
         * @param interaction    Interaction to append
         * @param onInteracted   Consumer for handling the interaction
         *
         * @return Builder
         */
        public Builder onInteraction(int actionRowIndex, Interaction interaction, Consumer<GroupedInteractionEvent> onInteracted) {
            ActionRow actionRow = interactiveRowedMessage.getActionRowByIndex(actionRowIndex);

            if (actionRow.getComponents().isEmpty()) {
                // Add new component

                switch (interaction.getType()) {
                    case BUTTON_CLICK:
                        actionRow.getComponents().add(interaction.getButton());
                        interactiveRowedMessage.interactions.put(interaction, onInteracted);
                        break;
                    case ENTITY_SELECT_MENU_OPTION_CLICK:
                    case STRING_SELECT_MENU_OPTION_CLICK:
                        throw new CannotAddInteractionException("Before adding any Select Menu Option interactions, you have to create Select Menu in action row index " + actionRowIndex + "!", interaction);
                    case MODAL_SUBMITTED:
                        throw new CannotAddInteractionException("This is no place for modal interactions!", interaction);
                }
            } else {
                // Edit existing component
                ItemComponent firstComponent = actionRow.getComponents().get(0);

                switch (firstComponent.getType()) {
                    case BUTTON:
                        if (!interaction.isButton()) {
                            throw new CannotAddInteractionException("On action row index " + actionRowIndex + " are buttons. Cannot add different component!", interaction);
                        }

                        if (actionRow.getComponents().size() >= Component.Type.BUTTON.getMaxPerRow()) {
                            throw new CannotAddInteractionException("Action row on index " + actionRowIndex + " reached maximum number of buttons (" + Component.Type.BUTTON.getMaxPerRow() + ")", interaction);
                        }

                        actionRow.getComponents().add(interaction.getButton());
                        interactiveRowedMessage.interactions.put(interaction, onInteracted);
                        break;
                    case STRING_SELECT:
                        if (!interaction.isSelectOption()) {
                            throw new CannotAddInteractionException("On action row index " + actionRowIndex + " is string select menu. Cannot add different component!", interaction);
                        }

                        StringSelectMenu selectMenu = (StringSelectMenu) firstComponent;

                        if (selectMenu.getOptions().size() >= StringSelectMenu.OPTIONS_MAX_AMOUNT) {
                            throw new CannotAddInteractionException("Action row on index " + actionRowIndex + " reached maximum number of select options (" + StringSelectMenu.OPTIONS_MAX_AMOUNT + ")", interaction);
                        }

                        actionRow.getComponents().clear();
                        // No real other option than just creating a copy
                        StringSelectMenu.Builder selectMenuBuilder = selectMenu.createCopy();
                        selectMenuBuilder.getOptions().removeIf(selectOption -> selectOption.getValue().equals(randomFillerValue));
                        actionRow.getComponents().add(selectMenuBuilder.addOptions(interaction.getSelectOption()).build());
                        interactiveRowedMessage.interactions.put(interaction, onInteracted);
                        break;
                    case USER_SELECT:
                    case ROLE_SELECT:
                    case MENTIONABLE_SELECT:
                    case CHANNEL_SELECT:
                        throw new CannotAddInteractionException("On action row index " + actionRowIndex + " is entity select menu. To listen for interactions, use #onEntitySelectMenuInteraction() method!", interaction);
                }

            }

            return this;
        }

        /**
         * Builds the {@link InteractiveRowedMessage}
         *
         * @return {@link InteractiveRowedMessage}
         */
        public InteractiveRowedMessage build() {
            return interactiveRowedMessage;
        }
    }
}
