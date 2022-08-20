package dev.mayuna.mayusjdautils.interactive;

import lombok.NonNull;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.EmojiUnion;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

public class Interaction {

    private EmojiUnion emojiUnion;
    private Button button;
    private SelectOption selectOption;

    private Interaction(EmojiUnion emojiUnion) {
        this.emojiUnion = emojiUnion;
    }

    private Interaction(Button button) {
        this.button = button;
    }

    private Interaction(SelectOption selectOption) {
        this.selectOption = selectOption;
    }

    /**
     * Creates {@link Interaction} with Emoji (unicode or emote)
     *
     * @param emojiUnion Emoji (unicode or emote), see {@link net.dv8tion.jda.api.JDA#getEmojiById(String)}
     *
     * @return {@link Interaction} object
     */
    public static Interaction asEmoji(EmojiUnion emojiUnion) {
        return new Interaction(emojiUnion);
    }

    /**
     * Creates {@link Interaction} with Button
     *
     * @param button Button, see JDA's wiki for how to construct Button
     *
     * @return {@link Interaction} object
     */
    public static Interaction asButton(@NonNull Button button) {
        // TODO: Všechny možnosti pro button
        return new Interaction(button);
    }

    /**
     * Creates {@link Interaction} with Select Option
     *
     * @param selectOption Select Option, see JDA's wiki for how to construct Select Option
     *
     * @return {@link Interaction} object
     */
    public static Interaction asSelectOption(SelectOption selectOption) {
        // TODO: Všechny možnosti pro select option
        return new Interaction(selectOption);
    }

    public boolean isEmoji() {
        return isUnicodeEmoji() || isCustomEmoji();
    }

    public boolean isUnicodeEmoji() {
        return emojiUnion != null && emojiUnion.getType() == Emoji.Type.UNICODE;
    }

    public boolean isCustomEmoji() {
        return emojiUnion != null && emojiUnion.getType() == Emoji.Type.CUSTOM;
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
        return isUnicodeEmoji() ? emojiUnion.asUnicode() : null;
    }

    /**
     * Gets Emote
     *
     * @return Returns null, if {@link Interaction} is not Custom Emoji
     */
    public CustomEmoji getCustomEmoji() {
        return isCustomEmoji() ? emojiUnion.asCustom() : null;
    }

    /**
     * Gets Emoji or Emote
     * @return Returns null if {@link Interaction} is not emoji (custom or unicode)
     */
    public Emoji getEmoji() {
        return emojiUnion;
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
        if (isUnicodeEmoji() || isCustomEmoji()) {
            return InteractionType.REACTION_ADD;
        }

        if (isButton()) {
            return InteractionType.BUTTON_CLICK;
        }

        if (isSelectOption()) {
            return InteractionType.SELECT_MENU_CLICK;
        }

        return InteractionType.UNKNOWN;
    }
}
