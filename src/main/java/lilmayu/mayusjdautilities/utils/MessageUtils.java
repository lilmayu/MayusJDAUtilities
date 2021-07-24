package lilmayu.mayusjdautilities.utils;

import lilmayu.mayusjdautilities.actions.InteractiveMessage;
import lilmayu.mayusjdautilities.actions.objects.MessageInteraction;
import lilmayu.mayusjdautilities.commands.MayuCommand;
import lilmayu.mayusjdautilities.settings.LanguageSettings;
import lilmayu.mayuslibrary.utils.objects.ParsedStackTraceElement;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.components.Button;

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

    // -- Closable -- //

    public static Message errorClosable(String text, MessageChannel messageChannel) {
        InteractiveMessage interactiveMessage = new InteractiveMessage(new MessageBuilder(error(text)));
        interactiveMessage.addInteraction(MessageInteraction.asButton(Button.danger("close", "Close")), interactiveMessage::delete);
        return interactiveMessage.sendMessage(messageChannel);
    }

    public static Message errorClosableEmbed(String text, MessageChannel messageChannel) {
        InteractiveMessage interactiveMessage = new InteractiveMessage(new MessageBuilder(errorEmbed(text)));
        interactiveMessage.addInteraction(MessageInteraction.asButton(Button.danger("close", "Close")), interactiveMessage::delete);
        return interactiveMessage.sendMessage(messageChannel);
    }

    public static Message warningClosable(String text, MessageChannel messageChannel) {
        InteractiveMessage interactiveMessage = new InteractiveMessage(new MessageBuilder(warning(text)));
        interactiveMessage.addInteraction(MessageInteraction.asButton(Button.danger("close", "Close")), interactiveMessage::delete);
        return interactiveMessage.sendMessage(messageChannel);
    }

    public static Message warningClosableEmbed(String text, MessageChannel messageChannel) {
        InteractiveMessage interactiveMessage = new InteractiveMessage(new MessageBuilder(warningEmbed(text)));
        interactiveMessage.addInteraction(MessageInteraction.asButton(Button.danger("close", "Close")), interactiveMessage::delete);
        return interactiveMessage.sendMessage(messageChannel);
    }

    public static Message informationClosable(String text, MessageChannel messageChannel) {
        InteractiveMessage interactiveMessage = new InteractiveMessage(new MessageBuilder(information(text)));
        interactiveMessage.addInteraction(MessageInteraction.asButton(Button.danger("close", "Close")), interactiveMessage::delete);
        return interactiveMessage.sendMessage(messageChannel);
    }

    public static Message informationClosableEmbed(String text, MessageChannel messageChannel) {
        InteractiveMessage interactiveMessage = new InteractiveMessage(new MessageBuilder(informationEmbed(text)));
        interactiveMessage.addInteraction(MessageInteraction.asButton(Button.danger("close", "Close")), interactiveMessage::delete);
        return interactiveMessage.sendMessage(messageChannel);
    }

    public static Message successfulClosable(String text, MessageChannel messageChannel) {
        InteractiveMessage interactiveMessage = new InteractiveMessage(new MessageBuilder(successful(text)));
        interactiveMessage.addInteraction(MessageInteraction.asButton(Button.danger("close", "Close")), interactiveMessage::delete);
        return interactiveMessage.sendMessage(messageChannel);
    }

    public static Message successfulClosableEmbed(String text, MessageChannel messageChannel) {
        InteractiveMessage interactiveMessage = new InteractiveMessage(new MessageBuilder(successfulEmbed(text)));
        interactiveMessage.addInteraction(MessageInteraction.asButton(Button.danger("close", "Close")), interactiveMessage::delete);
        return interactiveMessage.sendMessage(messageChannel);
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

        public static String asText(MayuCommand mayuCommand) {
            return error(LanguageSettings.Messages.getInvalidSyntaxHint().replace("{syntax}", mayuCommand.syntax)); // TODO: Special metoda
        }

        public static EmbedBuilder asEmbedBuilder(MayuCommand mayuCommand) {
            return errorEmbed(LanguageSettings.Messages.getInvalidSyntaxHint().replace("{syntax}", mayuCommand.syntax));
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
