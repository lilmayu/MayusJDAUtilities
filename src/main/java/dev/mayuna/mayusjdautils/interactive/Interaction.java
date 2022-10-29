package dev.mayuna.mayusjdautils.interactive;

import lombok.NonNull;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.UUID;

public class Interaction {

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

    public static Interaction asReactionAdd(@NonNull Emoji emoji) {
        return new Interaction(emoji);
    }

    /**
     * Creates {@link Interaction} with Button
     *
     * @param button Button, see JDA's wiki for how to construct Button
     *
     * @return {@link Interaction} object
     */
    public static Interaction asButton(@NonNull Button button) {
        return new Interaction(button);
    }

    public static Interaction asButton(@NonNull ButtonStyle buttonStyle, String label) {
        return new Interaction(Button.of(buttonStyle, UUID.randomUUID().toString(), label));
    }

    public static Interaction asButton(@NonNull ButtonStyle buttonStyle, String label, Emoji emoji) {
        return new Interaction(Button.of(buttonStyle, UUID.randomUUID().toString(), label, emoji));
    }

    public static Interaction asButton(@NonNull ButtonStyle buttonStyle, Emoji emoji) {
        return new Interaction(Button.of(buttonStyle, UUID.randomUUID().toString(), emoji));
    }

    /**
     * Creates {@link Interaction} with Select Option
     *
     * @param selectOption Select Option, see JDA's wiki for how to construct Select Option
     *
     * @return {@link Interaction} object
     */
    public static Interaction asSelectOption(@NonNull SelectOption selectOption) {
        return new Interaction(selectOption);
    }

    public static Interaction asSelectOption(@NonNull String label) {
        return new Interaction(SelectOption.of(label, UUID.randomUUID().toString()));
    }

    public boolean isEmoji() {
        return emoji != null;
    }

    public boolean isUnicodeEmoji() {
        return emoji != null && emoji.getType() == Emoji.Type.UNICODE;
    }

    public boolean isCustomEmoji() {
        return emoji != null && emoji.getType() == Emoji.Type.CUSTOM;
    }

    public boolean isButton() {
        return button != null;
    }

    public boolean isSelectOption() {
        return selectOption != null;
    }

    /**
     * Gets unicode Emoji
     *
     * @return Returns null, if {@link Interaction} is not Unicode Emoji
     */
    public UnicodeEmoji getUnicodeEmoji() {
        return isUnicodeEmoji() ? (UnicodeEmoji) emoji : null;
    }

    /**
     * Gets Emote
     *
     * @return Returns null, if {@link Interaction} is not Custom Emoji
     */
    public CustomEmoji getCustomEmoji() {
        return isCustomEmoji() ? (CustomEmoji) emoji : null;
    }

    /**
     * Gets Emoji or Emote
     *
     * @return Returns null if {@link Interaction} is not emoji (custom or unicode)
     */
    public Emoji getEmoji() {
        return isEmoji() ? emoji : null;
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
     * @return Non-null {@link InteractionType}, if {@link Interaction} is unicode or emoji, always returns {@link InteractionType#REACTION_ADD}
     */
    public InteractionType getType() {
        if (isEmoji()) {
            return InteractionType.REACTION_ADD;
        }

        if (isButton()) {
            return InteractionType.BUTTON_CLICK;
        }

        if (isSelectOption()) {
            return InteractionType.STRING_SELECT_MENU_OPTION_CLICK;
        }

        return InteractionType.UNKNOWN;
    }
}
