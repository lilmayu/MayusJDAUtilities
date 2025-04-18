package dev.mayuna.mayusjdautils.interactive;

import lombok.NonNull;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.UUID;

/**
 * Interaction that can be added to interactive messages
 */
public final class Interaction {

    private Emoji emoji;
    private Button button;
    private SelectOption selectOption;

    private Interaction(Emoji emoji) {
        this.emoji = emoji;
    }

    private Interaction(Button button) {
        this.button = button;
    }

    private Interaction(SelectOption selectOption) {
        this.selectOption = selectOption;
    }

    /**
     * Creates {@link Interaction} with Button, the ID will be randomized
     *
     * @param button Button, see JDA's wiki for how to construct Button
     *
     * @return {@link Interaction} object
     */
    public static Interaction asButton(@NonNull Button button) {
        return new Interaction(button.withId(UUID.randomUUID().toString()));
    }

    /**
     * Creates {@link Interaction} with Button, the ID will be randomized
     *
     * @param buttonStyle Button Style
     * @param label       Button Label
     *
     * @return {@link Interaction} object
     */
    public static Interaction asButton(@NonNull ButtonStyle buttonStyle, String label) {
        return new Interaction(Button.of(buttonStyle, UUID.randomUUID().toString(), label));
    }

    /**
     * Creates {@link Interaction} with Button, the ID will be randomized
     *
     * @param buttonStyle Button Style
     * @param label       Button Label
     * @param disabled    {@link Button#isDisabled()}
     *
     * @return {@link Interaction} object
     */
    public static Interaction asButton(@NonNull ButtonStyle buttonStyle, String label, boolean disabled) {
        return new Interaction(Button.of(buttonStyle, UUID.randomUUID().toString(), label).withDisabled(disabled));
    }


    /**
     * Creates {@link Interaction} with Button, the ID will be randomized
     *
     * @param buttonStyle Button Style
     * @param label       Button Label
     * @param emoji       Button Emoji
     *
     * @return {@link Interaction} object
     */
    public static Interaction asButton(@NonNull ButtonStyle buttonStyle, String label, Emoji emoji) {
        return new Interaction(Button.of(buttonStyle, UUID.randomUUID().toString(), label, emoji));
    }

    /**
     * Creates {@link Interaction} with Button, the ID will be randomized
     *
     * @param buttonStyle Button Style
     * @param label       Button Label
     * @param emoji       Button Emoji
     * @param disabled    {@link Button#isDisabled()}
     *
     * @return {@link Interaction} object
     */
    public static Interaction asButton(@NonNull ButtonStyle buttonStyle, String label, Emoji emoji, boolean disabled) {
        return new Interaction(Button.of(buttonStyle, UUID.randomUUID().toString(), label, emoji).withDisabled(disabled));
    }

    /**
     * Creates {@link Interaction} with Button, the ID will be randomized
     *
     * @param buttonStyle Button Style
     * @param emoji       Button Emoji
     *
     * @return {@link Interaction} object
     */
    public static Interaction asButton(@NonNull ButtonStyle buttonStyle, Emoji emoji) {
        return new Interaction(Button.of(buttonStyle, UUID.randomUUID().toString(), emoji));
    }

    /**
     * Creates {@link Interaction} with Button, the ID will be randomized
     *
     * @param buttonStyle Button Style
     * @param emoji       Button Emoji
     * @param disabled    {@link Button#isDisabled()}
     *
     * @return {@link Interaction} object
     */
    public static Interaction asButton(@NonNull ButtonStyle buttonStyle, Emoji emoji, boolean disabled) {
        return new Interaction(Button.of(buttonStyle, UUID.randomUUID().toString(), emoji).withDisabled(disabled));
    }

    /**
     * Creates {@link Interaction} with Select Option, the value will be randomized
     *
     * @param selectOption Select Option, see JDA's wiki for how to construct Select Option
     *
     * @return {@link Interaction} object
     */
    public static Interaction asSelectOption(@NonNull SelectOption selectOption) {
        ;
        return new Interaction(selectOption.withValue(UUID.randomUUID().toString()));
    }

    /**
     * Creates {@link Interaction} with Select Option, the value will be randomized
     *
     * @param label Select Option Label
     *
     * @return {@link Interaction} object
     */
    public static Interaction asSelectOption(@NonNull String label) {
        return new Interaction(SelectOption.of(label, UUID.randomUUID().toString()));
    }

    /**
     * Creates {@link Interaction} with Select Option, the value will be randomized
     *
     * @param label       Select Option Label
     * @param description Select Option description
     *
     * @return {@link Interaction} object
     */
    public static Interaction asSelectOption(@NonNull String label, @NonNull String description) {
        return new Interaction(SelectOption.of(label, UUID.randomUUID().toString()).withDescription(description));
    }

    /**
     * Creates {@link Interaction} with Select Option, the value will be randomized
     *
     * @param label       Select Option Label
     * @param description Select Option description
     * @param isDefault   {@link SelectOption}'s {@link SelectOption#isDefault()}
     *
     * @return {@link Interaction} object
     */
    public static Interaction asSelectOption(@NonNull String label, @NonNull String description, boolean isDefault) {
        return new Interaction(SelectOption.of(label, UUID.randomUUID().toString()).withDescription(description).withDefault(isDefault));
    }

    /**
     * Creates {@link Interaction} with Select Option, the value will be randomized
     *
     * @param label       Select Option Label
     * @param description Select Option description
     * @param isDefault   {@link SelectOption}'s {@link SelectOption#isDefault()}
     * @param emoji       Select Option Emoji
     *
     * @return {@link Interaction} object
     */
    public static Interaction asSelectOption(@NonNull String label, @NonNull String description, boolean isDefault, @NonNull Emoji emoji) {
        return new Interaction(SelectOption.of(label, UUID.randomUUID().toString())
                                           .withDescription(description)
                                           .withDefault(isDefault)
                                           .withEmoji(emoji));
    }

    /**
     * Creates {@link Interaction} with Select Option, the value will be randomized
     *
     * @param label       Select Option Label
     * @param description Select Option description
     * @param emoji       Select Option Emoji
     *
     * @return {@link Interaction} object
     */
    public static Interaction asSelectOption(@NonNull String label, @NonNull String description, @NonNull Emoji emoji) {
        return new Interaction(SelectOption.of(label, UUID.randomUUID().toString()).withDescription(description).withEmoji(emoji));
    }

    /**
     * Determines if {@link Interaction} is Button
     *
     * @return true if {@link Interaction} is Button
     */
    public boolean isButton() {
        return button != null;
    }

    /**
     * Determines if {@link Interaction} is Select Option
     *
     * @return true if {@link Interaction} is Select Option
     */
    public boolean isSelectOption() {
        return selectOption != null;
    }

    /**
     * Gets Button
     *
     * @return Returns null, if {@link Interaction} is not Button
     */
    public Button getButton() {
        return isButton() ? button : null;
    }

    /**
     * Gets Select Option
     *
     * @return Returns null, if {@link Interaction} is not Select Option
     */
    public SelectOption getSelectOption() {
        return isSelectOption() ? selectOption : null;
    }

    /**
     * Gets {@link Interaction}'s type
     *
     * @return Non-null {@link InteractionType}
     */
    public InteractionType getType() {
        if (isButton()) {
            return InteractionType.BUTTON_CLICK;
        }

        if (isSelectOption()) {
            return InteractionType.STRING_SELECT_MENU_OPTION_CLICK;
        }

        return InteractionType.UNKNOWN;
    }
}
