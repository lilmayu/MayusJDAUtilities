package lilmayu.mayusjdautilities.utils;

import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Instant;

public class GeneralUtils {

    public static Object getLast(Object[] array) {
        if (array.length == 0)
            return null;
        return array[array.length - 1];
    }

    public static String makePrettyList(Object[] array) {
        if (array.length == 0)
            return "";
        String fullString = "";
        for (Object object : array) {
            fullString += object.toString();
            if (!object.equals(getLast(array))) {
                fullString += ", ";
            }
        }
        return fullString;
    }

    public static EmbedBuilder makeDefaultEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setFooter("Powered by MayuJDAUtils");
        embedBuilder.setTimestamp(Instant.now());
        return embedBuilder;
    }
}
