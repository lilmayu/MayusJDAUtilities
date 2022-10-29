package dev.mayuna.mayusjdautils.util;

import dev.mayuna.mayusjdautils.lang.LanguageSettings;
import dev.mayuna.mayuslibrary.util.objects.ParsedStackTraceElement;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

public class MessageInfo {

    public static boolean useSystemEmotes = false;

    public static String error(String content) {
        return useSystemEmotes ? SystemEmote.ERROR + " | " + content : "❌ | " + content;
    }

    public static String warning(String content) {
        return useSystemEmotes ? SystemEmote.WARNING + " | " + content : "❗ | " + content;
    }

    public static String information(String content) {
        return useSystemEmotes ? SystemEmote.INFORMATION + " | " + content : "❔ | " + content;
    }

    public static String success(String content) {
        return useSystemEmotes ? SystemEmote.SUCCESS + " | " + content : "✅ | " + content;
    }

    public static EmbedBuilder errorEmbed(String content, MessageEmbed.Field... fields) {
        EmbedBuilder embedBuilder = quickEmbed(ColorUtils.getError(), useSystemEmotes ? SystemEmote.ERROR + " Error" : "❌ Error", content);

        if (fields != null) {
            for (MessageEmbed.Field field : fields) {
                embedBuilder.addField(field);
            }
        }

        return embedBuilder;
    }

    public static EmbedBuilder warningEmbed(String content, MessageEmbed.Field... fields) {
        EmbedBuilder embedBuilder = quickEmbed(ColorUtils.getWarning(), useSystemEmotes ? SystemEmote.WARNING + " Warning" : "❗ Warning", content);

        if (fields != null) {
            for (MessageEmbed.Field field : fields) {
                embedBuilder.addField(field);
            }
        }

        return embedBuilder;
    }

    public static EmbedBuilder informationEmbed(String content, MessageEmbed.Field... fields) {
        EmbedBuilder embedBuilder = quickEmbed(ColorUtils.getInformation(), useSystemEmotes ? SystemEmote.INFORMATION + " Information" : "❔ Information", content);

        if (fields != null) {
            for (MessageEmbed.Field field : fields) {
                embedBuilder.addField(field);
            }
        }

        return embedBuilder;
    }

    public static EmbedBuilder successEmbed(String content, MessageEmbed.Field... fields) {
        EmbedBuilder embedBuilder = quickEmbed(ColorUtils.getSuccess(), useSystemEmotes ? SystemEmote.SUCCESS + " Success" : "✅ Success", content);

        if (fields != null) {
            for (MessageEmbed.Field field : fields) {
                embedBuilder.addField(field);
            }
        }

        return embedBuilder;
    }

    private static EmbedBuilder quickEmbed(Color color, String title, String text) {
        return DiscordUtils.getDefaultEmbed().setColor(color).setTitle(title).setDescription(text);
    }

    public static String formatExceptionStackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);

        StringBuilder text = new StringBuilder();

        for (String line : stringWriter.toString().split("\n")) {
            if (text.length() + line.length() > 2048) {
                break;
            }

            text.append(line).append("\n");
        }

        return text.toString();
    }

    public static String formatExceptionInformationField(Throwable throwable) {
        ParsedStackTraceElement parsedStackTraceElement = new ParsedStackTraceElement(throwable.getStackTrace()[0]);

        String string = "```md";

        string += "Exception: " + throwable + "\n";
        string += " - Class.: " + parsedStackTraceElement.getClassName() + "\n";
        string += " - Method: #" + parsedStackTraceElement.getMethodName() + "()\n";
        string += " - File..: " + parsedStackTraceElement.getFileName() + "\n";
        string += " - Line..: " + parsedStackTraceElement.getLineNumber() + "\n";

        return string + "```";
    }

    public static void sendExceptionMessage(MessageChannel messageChannel, Throwable throwable) {
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();

        messageCreateBuilder.setEmbeds(errorEmbed(LanguageSettings.Messages.getExceptionOccurredMessage()).addField(LanguageSettings.Other.getInformation(),
                                                                                                                    formatExceptionInformationField(throwable),
                                                                                                                    false
        ).build());

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);

        messageCreateBuilder.addFiles(FileUpload.fromData(stringWriter.toString().getBytes(StandardCharsets.UTF_8), "exception.txt"));
        messageChannel.sendMessage(messageCreateBuilder.build()).queue();
    }
}
