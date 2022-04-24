package dev.mayuna.mayusjdautils.utils;

import dev.mayuna.mayusjdautils.data.MayuCoreListener;
import dev.mayuna.mayusjdautils.lang.LanguageSettings;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.*;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.time.Instant;
import java.util.Random;

public class DiscordUtils {

    private static @Setter EmbedBuilder defaultEmbed = new EmbedBuilder().setFooter("Powered by Mayu's JDA Utilities")
            .setTimestamp(Instant.now())
            .setTitle("Loading...")
            .setDescription("Please wait.");

    private static @Getter @Setter MessageBuilder defaultMessageBuilder = new MessageBuilder().setEmbeds(getDefaultEmbed().build());

    /**
     * Checks if specified string is qualified as User mention
     *
     * @param string Non-null string
     *
     * @return true if it is qualified as User mention
     */
    public static boolean isUserMention(@NonNull String string) {
        return User.USER_TAG.matcher(string).matches();
    }

    /**
     * Checks if specified string is qualified as Role mention
     *
     * @param string Non-null string
     *
     * @return true if it is qualified as Role mention
     */
    public static boolean isRoleMention(@NonNull String string) {
        return string.matches("<@&(\\d{17,19})>");
    }

    /**
     * Checks if specified string is qualified as Channel mention
     *
     * @param string Non-null string
     *
     * @return true if it is qualified as Channel mention
     */
    public static boolean isChannelMention(@NonNull String string) {
        return string.matches("<#(\\d{17,19})>");
    }

    /**
     * Checks if specified string is qualified as Emote mention
     *
     * @param string Non-null string
     *
     * @return true if it is qualified as Emote mention
     */
    public static boolean isEmoteMention(@NonNull String string) {
        return string.startsWith("<:") && string.endsWith(">");
    }

    /**
     * Gets ID from any mention (except emote)
     *
     * @param string Non-null string
     *
     * @return Returns ID of specified mention
     */
    public static String getMentionID(@NonNull String string) {
        return string.replace("<", "").replace("#", "").replace("&", "").replace("@", "").replace(">", "").replace(" ", "");
    }

    /**
     * Gets Default embed with current Timestamp
     *
     * @return Non-null {@link EmbedBuilder}
     */
    public static EmbedBuilder getDefaultEmbed() {
        return new EmbedBuilder(defaultEmbed).setTimestamp(Instant.now());
    }

    /**
     * Generates {@link Button} with {@link ButtonStyle} and label with random ID
     *
     * @param buttonStyle {@link ButtonStyle}
     * @param label       Label
     *
     * @return Non-null {@link Button}
     */
    public static Button generateButton(ButtonStyle buttonStyle, String label) {
        return Button.of(buttonStyle, Integer.toString(new Random().nextInt()), label);
    }

    /**
     * Generates {@link Button} with {@link ButtonStyle} and label with generic button close ID which this library uses
     *
     * @param buttonStyle {@link ButtonStyle}
     * @param label       Label
     *
     * @return Non-null {@link Button}
     */
    public static Button generateCloseButton(ButtonStyle buttonStyle, String label) {
        return Button.of(buttonStyle, MayuCoreListener.GENERIC_BUTTON_CLOSE_ID, label);
    }

    /**
     * Generates {@link Button} with {@link ButtonStyle} and label which is specified in LanguageSettings.Other#getClose() and generic close button ID
     *
     * @param buttonStyle {@link ButtonStyle}
     *
     * @return Non-null {@link Button}
     */
    public static Button generateCloseButton(ButtonStyle buttonStyle) {
        return generateCloseButton(buttonStyle, LanguageSettings.Other.getClose());
    }

    /**
     * Generates {@link SelectOption} with specified label and with random ID
     *
     * @param label Label
     *
     * @return Non-null {@link SelectOption}
     */
    public static SelectOption generateSelectOption(String label) {
        return SelectOption.of(label, Integer.toString(new Random().nextInt()));
    }

    /**
     * Generates {@link SelectOption} with specified label and with generic button close ID which this library uses
     *
     * @param label Label
     *
     * @return Non-null {@link SelectOption}
     */
    public static SelectOption generateCloseSelectOption(String label) {
        return SelectOption.of(label, MayuCoreListener.GENERIC_BUTTON_CLOSE_ID);
    }

    /**
     * Generates {@link SelectOption} with label which is specified in LanguageSettings.Other#getClose() and generic close button ID
     *
     * @return Non-null {@link SelectOption}
     */
    public static SelectOption generateCloseSelectOption() {
        return SelectOption.of(LanguageSettings.Other.getClose(), MayuCoreListener.GENERIC_BUTTON_CLOSE_ID);
    }

    public static boolean isDiscordException(Throwable throwable) {
        if (throwable instanceof MissingAccessException) {
            return true;
        }

        if (throwable instanceof InsufficientPermissionException) {
            return true;
        }

        if (throwable instanceof InteractionFailureException) {
            return true;
        }

        if (throwable instanceof PermissionException) {
            return true;
        }

        if (throwable instanceof RateLimitedException) {
            return true;
        }

        if (throwable instanceof ErrorResponseException) {
            ErrorResponseException responseException = (ErrorResponseException) throwable;
            return responseException.getErrorCode() > 10000;
        }

        return false;
    }
}
