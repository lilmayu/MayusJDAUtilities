package lilmayu.mayusjdautilities.utils;

import lilmayu.mayusjdautilities.commands.MayuCommand;
import lilmayu.mayusjdautilities.interactive.InteractionType;
import lilmayu.mayusjdautilities.interactive.InteractiveMessage;
import lilmayu.mayusjdautilities.interactive.objects.MessageInteraction;
import lilmayu.mayusjdautilities.settings.LanguageSettings;
import lilmayu.mayuslibrary.utils.objects.ParsedStackTraceElement;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class MessageUtils {

    public static String error(String text) {
        return SystemEmotes.ERROR + " | " + text;
    }

    // -- General -- //

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

    private static EmbedBuilder quickEmbed(Color color, String title, String text) {
        return DiscordUtils.getDefaultEmbed().setColor(color).setTitle(title).setDescription(text);
    }

    // -- Others -- //

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

        messageBuilder.setEmbed(errorEmbed(LanguageSettings.Messages.getExceptionOccurredMessage()).addField(LanguageSettings.Other
                .getInformation(), formatExceptionInformationField(throwable), false).build());

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);

        messageChannel.sendMessage(messageBuilder.build())
                .addFile(stringWriter.toString().getBytes(StandardCharsets.UTF_8), "exception.txt")
                .complete();
    }

    public static void send(String text, MessageChannel messageChannel) {
        messageChannel.sendMessage(text).complete();
    }

    public static void send(EmbedBuilder embedBuilder, MessageChannel messageChannel) {
        messageChannel.sendMessage(embedBuilder.build()).complete();
    }

    public static class Builder {

        private @Getter InteractiveMessage interactiveMessage = new InteractiveMessage(new MessageBuilder("Loading..."));

        private @Getter Type type;
        private @Getter String content;
        private @Getter int deleteAfter;
        private @Getter boolean closable;
        private @Getter boolean embed;

        public static Builder create() {
            return new Builder();
        }

        public Builder setType(Type type) {
            this.type = type;

            return this;
        }

        public Builder setContent(String content) {
            this.content = content;

            return this;
        }

        public Builder setDeleteAfter(int seconds) {
            this.deleteAfter = seconds;

            return this;
        }

        public Builder setEmbed(boolean embed) {
            this.embed = embed;

            return this;
        }

        public Builder setClosable(boolean closable) {
            this.closable = closable;

            return this;
        }

        public Builder addInteraction(MessageInteraction messageInteraction, Runnable runnable) {
            interactiveMessage.addInteraction(messageInteraction, runnable);

            return this;
        }

        public MessageBuilder buildAsMessageBuilder() {
            return generateMessageBuilder();
        }

        public Message send(MessageChannel messageChannel) {
            interactiveMessage.setMessageBuilder(generateMessageBuilder());

            if (closable) {
                if (interactiveMessage.getInteractions(InteractionType.SELECTION_MENU).size() == 0 && interactiveMessage
                        .getInteractions(InteractionType.BUTTON)
                        .size() < 25) {
                    interactiveMessage.addInteraction(MessageInteraction.asButton(Button.danger("close", "Close")), interactiveMessage::delete);
                }
            }

            Message message = interactiveMessage.sendMessage(messageChannel);

            if (deleteAfter > 0) {
                message.delete().queueAfter(deleteAfter, TimeUnit.SECONDS, success -> {
                    // Ignore
                }, failure -> {
                    // Ignore
                });
            }

            return message;
        }

        private MessageBuilder generateMessageBuilder() {
            MessageBuilder messageBuilder = new MessageBuilder();

            if (embed) {
                switch (type) {
                    case INFORMATION:
                        messageBuilder.setEmbeds(informationEmbed(content).build());
                        break;
                    case ERROR:
                        messageBuilder.setEmbeds(errorEmbed(content).build());
                        break;
                    case SUCCESSFUL:
                        messageBuilder.setEmbeds(successfulEmbed(content).build());
                        break;
                }
            } else {
                switch (type) {
                    case INFORMATION:
                        messageBuilder.setContent(information(content));
                        break;
                    case ERROR:
                        messageBuilder.setContent(error(content));
                        break;
                    case SUCCESSFUL:
                        messageBuilder.setContent(successful(content));
                        break;
                }
            }

            return messageBuilder;
        }

        public enum Type {
            INFORMATION, ERROR, SUCCESSFUL
        }
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
            return error(LanguageSettings.Messages.getInvalidSyntaxHint()
                    .replace("{syntax}", mayuCommand.syntax)); // TODO: Special metoda
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
