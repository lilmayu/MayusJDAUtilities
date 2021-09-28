package dev.mayuna.mayusjdautils.utils;

import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import java.time.Instant;

public class DiscordUtils {

    private static @Setter EmbedBuilder defaultEmbed = new EmbedBuilder().setFooter("Powered by Mayu's JDA Utilities")
            .setTimestamp(Instant.now())
            .setTitle("Loading...")
            .setDescription("Please wait.");

    /**
     * Checks if specified string is qualified as User mention
     *
     * @param string Non-null string
     * @return true if it is qualified as User mention
     */
    public static boolean isUserMention(@NonNull String string) {
        return string.matches("<@!?(\\d{17,19})>");
    }

    /**
     * Checks if specified string is qualified as Role mention
     *
     * @param string Non-null string
     * @return true if it is qualified as Role mention
     */
    public static boolean isRoleMention(@NonNull String string) {
        return string.matches("<@&(\\d{17,19})>");
    }

    /**
     * Checks if specified string is qualified as Channel mention
     *
     * @param string Non-null string
     * @return true if it is qualified as Channel mention
     */
    public static boolean isChannelMention(@NonNull String string) {
        return string.matches("<#(\\d{17,19})>");
    }

    /**
     * Checks if specified string is qualified as Emote mention
     *
     * @param string Non-null string
     * @return true if it is qualified as Emote mention
     */
    public static boolean isEmoteMention(@NonNull String string) {
        return string.startsWith("<:") && string.endsWith(">");
    }

    /**
     * Gets ID from any mention
     *
     * @param string Non-null string
     * @return Returns ID of specified mention
     */
    public static String getMentionID(@NonNull String string) {
        return string.replace("<", "")
                .replace("#", "")
                .replace("&", "")
                .replace("@", "")
                .replace(">", "")
                .replace(" ", "");
    }

    /**
     * Gets Default embed with current Timestamp
     *
     * @return Non-null {@link EmbedBuilder}
     */
    public static EmbedBuilder getDefaultEmbed() {
        return new EmbedBuilder(defaultEmbed).setTimestamp(Instant.now());
    }
}
