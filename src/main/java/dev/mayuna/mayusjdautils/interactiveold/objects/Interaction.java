package dev.mayuna.mayusjdautils.interactiveold.objects;

import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

public class Interaction {

    private MessageReaction.ReactionEmote reactionEmote;
    private Button button;
    private SelectOption selectOption;

    private Interaction(String emoji, JDA jda) {
        this.reactionEmote = MessageReaction.ReactionEmote.fromUnicode(emoji, jda);
    }

    private Interaction(Emote emote) {
        this.reactionEmote = MessageReaction.ReactionEmote.fromCustom(emote);
    }

    private Interaction(Button button) {
        this.button = button;
    }

    private Interaction(SelectOption selectOption) {
        this.selectOption = selectOption;
    }

    /**
     * Creates MessageInteraction with Emoji (NOTE: Use unicode for emoji value)
     *
     * @param emoji Emoji in unicode format
     * @param jda   JDA object
     *
     * @return MessageInteraction object, which can be added into InteractiveMessage
     */
    public static Interaction asEmoji(@NonNull String emoji, @NonNull JDA jda) {
        return new Interaction(emoji, jda);
    }

    /**
     * Creates MessageInteraction with Emote
     *
     * @param emote Emote
     *
     * @return MessageInteraction object, which can be added into InteractiveMessage
     */
    public static Interaction asEmote(@NonNull Emote emote) {
        return new Interaction(emote);
    }

    /**
     * Creates MessageInteraction with Button
     *
     * @param button Button, see JDA's wiki for how to construct Button
     *
     * @return MessageInteraction object, which can be added into InteractiveMessage
     */
    public static Interaction asButton(@NonNull Button button) {
        return new Interaction(button);
    }

    /**
     * Creates MessageInteraction with Select Option
     *
     * @param selectOption Select Option, see JDA's wiki for how to construct Select Option
     *
     * @return MessageInteraction object, which can be added into InteractiveMessage
     */
    public static Interaction asSelectOption(SelectOption selectOption) {
        return new Interaction(selectOption);
    }

    public boolean isEmoji() {
        return reactionEmote != null && reactionEmote.isEmoji();
    }

    public boolean isEmote() {
        return reactionEmote != null && reactionEmote.isEmote();
    }

    public boolean isButton() {
        return button != null;
    }

    public boolean isSelectOption() {
        return selectOption != null;
    }

    /**
     * Gets Emoji
     *
     * @return Returns null, if MessageInteraction is not Emoji
     */
    public String getEmoji() {
        return isEmoji() ? reactionEmote.getEmoji() : null;
    }

    /**
     * Gets Emote
     *
     * @return Returns null, if MessageInteraction is not Emote
     */
    public Emote getEmote() {
        return isEmote() ? reactionEmote.getEmote() : null;
    }

    /**
     * Gets Button
     *
     * @return Returns null, if MessageInteraction is not Button
     */
    public Button getButton() {
        return isButton() ? button : null;
    }

    /**
     * Gets Select Option
     *
     * @return Returns null, if MessageInteraction is not Select Option
     */
    public SelectOption getSelectOption() {
        return isSelectOption() ? selectOption : null;
    }

    /**
     * Gets MessageInteraction's type
     *
     * @return InteractionType (NOTE: {@link InteractionType#REACTION} is for Emoji and Emote; Type {@link InteractionType#SELECT_MENU} is Select Option)
     */
    public InteractionType getInteractionType() {
        if (isEmoji() || isEmote())
            return InteractionType.REACTION;

        if (isButton())
            return InteractionType.BUTTON;

        if (isSelectOption())
            return InteractionType.SELECT_MENU;

        return null;
    }
}
