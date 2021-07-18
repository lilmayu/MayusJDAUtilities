package lilmayu.mayusjdautilities.utils;

import lilmayu.mayusjdautilities.settings.LanguageSettings;
import lilmayu.mayuslibrary.utils.objects.ParsedStackTraceElement;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class MessageUtils {

    // -- General -- //

    public static String error(String text) {
        return SystemEmotes.ERROR + " | " + text;
    }

    public static EmbedBuilder errorEmbed(String text) {
        return quickEmbed(ColorUtils.getErrorColor(), SystemEmotes.ERROR + " Error", text);
    }

    public static String warning(String text) {
        return SystemEmotes.WARNING + " | " + text;
    }

    public static EmbedBuilder warningEmbed(String text) {
        return quickEmbed(ColorUtils.getWarningColor(), SystemEmotes.WARNING + " Warning", text);
    }

    public static String information(String text) {
        return SystemEmotes.INFORMATION + " | " + text;
    }

    public static EmbedBuilder informationEmbed(String text) {
        return quickEmbed(ColorUtils.getInformationColor(), SystemEmotes.INFORMATION + " Information", text);
    }

    public static String successful(String text) {
        return SystemEmotes.SUCCESSFUL + " | " + text;
    }

    public static EmbedBuilder successfulEmbed(String text) {
        return quickEmbed(ColorUtils.getSuccessfulColor(), SystemEmotes.SUCCESSFUL + " Success", text);
    }

    // -- Others -- //

    private static EmbedBuilder quickEmbed(Color color, String title, String text) {
        return DiscordUtils.getDefaultEmbed().setColor(color).setTitle(title).setDescription(text);
    }

    private static String formatExceptionInformationField(Throwable throwable) {
        ParsedStackTraceElement parsedStackTraceElement = new ParsedStackTraceElement(throwable.getStackTrace()[0]);

        String string = "```";

        string += "Exception: " + throwable + "\n";
        string += " - Class.: " + parsedStackTraceElement.getClassName() + "\n";
        string += " - Method: #" + parsedStackTraceElement.getMethodName() + "()\n";
        string += " - File..: " + parsedStackTraceElement.getFileName() + "\n";
        string += " - Line..: " + parsedStackTraceElement.getLineNumber() + "\n";

        return string + "```";
    }

    public static void sendExceptionMessage(MessageChannel messageChannel, Throwable throwable) {
        MessageBuilder messageBuilder = new MessageBuilder();

        messageBuilder.setEmbed(errorEmbed(LanguageSettings.Messages.getExceptionOccurredMessage())
                .addField(LanguageSettings.Other.getInformation(), formatExceptionInformationField(throwable), false).build());

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);

        messageChannel.sendMessage(messageBuilder.build())
                .addFile(stringWriter.toString().getBytes(StandardCharsets.UTF_8), "exception.txt").complete();
    }

    public static void send(String text, MessageChannel messageChannel) {
        messageChannel.sendMessage(text).complete();
    }

    public static void send(EmbedBuilder embedBuilder, MessageChannel messageChannel) {
        messageChannel.sendMessage(embedBuilder.build()).complete();
    }

    // -- Language Specific -- //

    public static class InvalidSyntax {

        public static String asText() {
            return error(LanguageSettings.Messages.getInvalidSyntax());
        }

        public static EmbedBuilder asEmbedBuilder() {
            return errorEmbed(LanguageSettings.Messages.getInvalidSyntax());
        }
    }

    public static class UnknownCommand {

        public static String asText() {
            return error(LanguageSettings.Messages.getUnknownCommand());
        }

        public static EmbedBuilder asEmbedBuilder() {
            return errorEmbed(LanguageSettings.Messages.getUnknownCommand());
        }
    }
}
