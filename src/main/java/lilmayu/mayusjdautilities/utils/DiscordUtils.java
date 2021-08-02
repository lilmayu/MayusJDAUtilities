package lilmayu.mayusjdautilities.utils;

import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Instant;

public class DiscordUtils {

    private static @Setter EmbedBuilder defaultEmbed = new EmbedBuilder().setFooter("Powered by Mayu's JDA Utilities")
            .setTimestamp(Instant.now())
            .setTitle("Loading...")
            .setDescription("Please wait.");

    public static boolean isUserMention(String string) {
        return string.matches("<@!?(\\d{17,19})>");
    }

    public static boolean isRoleMention(String string) {
        return string.matches("<@&(\\d{17,19})>");
    }

    public static boolean isChannelMention(String string) {
        return string.matches("<#(\\d{17,19})>");
    }

    public static boolean isEmoteMention(String string) {
        return string.startsWith("<:") && string.endsWith(">");
    }

    public static String getEmoteLink(long emoteID) {
        return "https://cdn.discordapp.com/emojis/" + emoteID + ".png";
    }

    public static String getMentionID(String string) {
        return string.replace("<", "")
                .replace("#", "")
                .replace("&", "")
                .replace("@", "")
                .replace(">", "")
                .replace(" ", "");
    }

    public static EmbedBuilder getDefaultEmbed() {
        return new EmbedBuilder(defaultEmbed).setTimestamp(Instant.now());
    }
}
