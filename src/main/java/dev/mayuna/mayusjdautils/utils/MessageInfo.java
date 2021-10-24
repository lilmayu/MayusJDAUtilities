package dev.mayuna.mayusjdautils.utils;

import dev.mayuna.mayusjdautils.commands.MayuCommand;
import dev.mayuna.mayusjdautils.data.MayuCoreListener;
import dev.mayuna.mayusjdautils.interactive.InteractionType;
import dev.mayuna.mayusjdautils.interactive.InteractiveMessage;
import dev.mayuna.mayusjdautils.interactive.objects.Interaction;
import dev.mayuna.mayusjdautils.lang.LanguageSettings;
import dev.mayuna.mayuslibrary.utils.objects.ParsedStackTraceElement;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MessageInfo {

    public static boolean useSystemEmotes = false;

    public static String error(String content) {
        return useSystemEmotes ? SystemEmotes.ERROR + " | " + content : "❌ | " + content;
    }

    public static String warning(String content) {
        return useSystemEmotes ? SystemEmotes.WARNING + " | " + content : "❗ | " + content;
    }

    public static String information(String content) {
        return useSystemEmotes ? SystemEmotes.INFORMATION + " | " + content : "❔ | " + content;
    }

    public static String success(String content) {
        return useSystemEmotes ? SystemEmotes.SUCCESS + " | " + content : "✅ | " + content;
    }

    public static EmbedBuilder errorEmbed(String content) {
        return quickEmbed(ColorUtils.getError(), useSystemEmotes ? SystemEmotes.ERROR + " Error" : "❌ Error", content);
    }

    public static EmbedBuilder warningEmbed(String content) {
        return quickEmbed(ColorUtils.getWarning(), useSystemEmotes ? SystemEmotes.WARNING + " Warning" : "❗ Warning", content);
    }

    public static EmbedBuilder informationEmbed(String content) {
        return quickEmbed(ColorUtils.getInformation(), useSystemEmotes ? SystemEmotes.INFORMATION + " Information" : "❔ Information", content);
    }

    public static EmbedBuilder successEmbed(String content) {
        return quickEmbed(ColorUtils.getSuccess(), useSystemEmotes ? SystemEmotes.SUCCESS + " Success" : "✅ Success", content);
    }

    public static Builder closable(Type type, String content, int closeAfterSeconds) {
        return Builder.create().setType(type).setContent(content).setClosable(true).setCloseAfterSeconds(closeAfterSeconds);
    }

    private static EmbedBuilder quickEmbed(Color color, String title, String text) {
        return DiscordUtils.getDefaultEmbed().setColor(color).setTitle(title).setDescription(text);
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
        MessageBuilder messageBuilder = new MessageBuilder();

        messageBuilder.setEmbed(errorEmbed(LanguageSettings.Messages.getExceptionOccurredMessage()).addField(LanguageSettings.Other.getInformation(),
                formatExceptionInformationField(throwable),
                false).build());

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);

        messageChannel.sendMessage(messageBuilder.build()).addFile(stringWriter.toString().getBytes(StandardCharsets.UTF_8), "exception.txt").complete();
    }

    public enum Type {
        ERROR, WARNING, INFORMATION, SUCCESS, CUSTOM
    }

    public static class Builder {

        private final @Getter List<User> interactionWhitelist = new ArrayList<>();
        // Huge
        private final @Getter InteractiveMessage interactiveMessage = InteractiveMessage.create();
        private @Getter Type type;
        private @Getter boolean embed;
        private @Getter boolean closable;
        // Data
        private @Getter String content;
        private @Getter int closeAfterSeconds;
        private @Getter SelectionMenu.Builder selectionMenuBuilder;

        // Overrides
        private @Getter EmbedBuilder customEmbedBuilder;
        private @Getter Color customColor;

        public static Builder create() {
            return new Builder();
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setType(Type type) {
            this.type = type;
            return this;
        }

        public Builder setEmbed(boolean flag) {
            this.embed = flag;
            return this;
        }

        public Builder setClosable(boolean flag) {
            this.closable = flag;
            return this;
        }

        public Builder addOnInteractionWhitelist(User user) {
            this.interactionWhitelist.add(user);
            return this;
        }

        public Builder setCloseAfterSeconds(int seconds) {
            this.closeAfterSeconds = seconds;
            return this;
        }

        public Builder setOverrideEmbed(EmbedBuilder embedBuilder) {
            this.customEmbedBuilder = embedBuilder;
            this.type = Type.CUSTOM;
            return this;
        }

        /**
         * You can use pre-defined colors in {@link ColorUtils}
         *
         * @param color Color
         *
         * @return Builder
         */
        public Builder setOverrideColor(Color color) {
            this.customColor = color;
            return this;
        }

        public Builder setSelectionMenuBuilder(SelectionMenu.Builder selectionMenuBuilder) {
            this.selectionMenuBuilder = selectionMenuBuilder;
            return this;
        }

        public Builder generateSelectionMenuBuilder(String placeholder) {
            this.selectionMenuBuilder = SelectionMenu.create(Integer.toString(new Random().nextInt())).setPlaceholder(placeholder);
            return this;
        }

        public Builder addInteraction(Interaction interaction, Runnable runnable) {
            interactiveMessage.addInteraction(interaction, runnable);
            return this;
        }

        private void prepareMessage() {
            MessageBuilder messageBuilder = new MessageBuilder();

            if (type != Type.CUSTOM) {
                if (embed) {
                    EmbedBuilder embedBuilder = new EmbedBuilder();

                    switch (type) {
                        case ERROR:
                            embedBuilder = errorEmbed(content);
                            break;
                        case WARNING:
                            embedBuilder = warningEmbed(content);
                            break;
                        case INFORMATION:
                            embedBuilder = informationEmbed(content);
                            break;
                        case SUCCESS:
                            embedBuilder = successEmbed(content);
                            break;
                    }

                    if (customColor != null) {
                        embedBuilder.setColor(customColor);
                    }

                    messageBuilder.setEmbeds(embedBuilder.build());
                } else {
                    switch (type) {
                        case ERROR:
                            messageBuilder.setContent(error(content));
                            break;
                        case WARNING:
                            messageBuilder.setContent(warning(content));
                            break;
                        case INFORMATION:
                            messageBuilder.setContent(information(content));
                            break;
                        case SUCCESS:
                            messageBuilder.setContent(success(content));
                            break;
                    }
                }
            } else {
                if (customColor != null) {
                    customEmbedBuilder.setColor(customColor);
                }

                messageBuilder.setEmbeds(customEmbedBuilder.build());
            }

            interactiveMessage.setMessageBuilder(messageBuilder);
            interactiveMessage.setSelectionMenuBuilder(selectionMenuBuilder);
            interactiveMessage.setDeleteAfterSeconds(closeAfterSeconds);

            if (!interactionWhitelist.isEmpty()) {
                interactiveMessage.setWhitelistUsers(true);
                interactionWhitelist.forEach(interactiveMessage::addWhitelistUser);
            }

            if (closable) {
                if (interactiveMessage.getInteractions(InteractionType.BUTTON).size() < 25 || interactiveMessage.getInteractions(InteractionType.SELECTION_MENU).size() < 25) {
                    if (interactiveMessage.getSelectionMenuBuilder() == null) {
                        interactiveMessage.addInteraction(Interaction.asButton(Button.danger(MayuCoreListener.GENERIC_BUTTON_CLOSE_ID, LanguageSettings.Other.getClose())),
                                interactiveMessage::delete);
                    } else {
                        interactiveMessage.addInteraction(Interaction.asSelectOption(SelectOption.of(LanguageSettings.Other.getClose(), MayuCoreListener.GENERIC_BUTTON_CLOSE_ID)),
                                interactiveMessage::delete);
                    }
                }
            }
        }

        public Message send(MessageChannel messageChannel) {
            prepareMessage();
            return interactiveMessage.send(messageChannel);
        }

        public Message send(InteractionHook interactionHook) {
            prepareMessage();
            return interactiveMessage.send(interactionHook);
        }
    }

    public static class InvalidSyntax {

        public static String asText() {
            return error(LanguageSettings.Messages.getInvalidSyntax());
        }

        public static EmbedBuilder asEmbedBuilder() {
            return errorEmbed(LanguageSettings.Messages.getInvalidSyntax());
        }

        public static String asText(MayuCommand mayuCommand) {
            return error(LanguageSettings.Messages.getInvalidSyntaxHint().replace("{syntax}", mayuCommand.syntax));
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
