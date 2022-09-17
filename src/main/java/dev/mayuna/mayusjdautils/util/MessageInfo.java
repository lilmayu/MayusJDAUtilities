package dev.mayuna.mayusjdautils.util;

import dev.mayuna.mayusjdautils.interactive.GroupedInteractionEvent;
import dev.mayuna.mayusjdautils.interactive.Interaction;
import dev.mayuna.mayusjdautils.interactive.components.InteractiveMessage;
import dev.mayuna.mayusjdautils.lang.LanguageSettings;
import dev.mayuna.mayuslibrary.util.objects.ParsedStackTraceElement;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

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

    public enum Type {
        ERROR,
        WARNING,
        INFORMATION,
        SUCCESS,
        CUSTOM
    }

    public static class Builder {

        private final @Getter List<User> interactionWhitelist = new ArrayList<>();
        // Huge
        private final @Getter InteractiveMessage interactiveMessage = InteractiveMessage.create(new MessageEditBuilder());
        private @Getter final List<MessageEmbed.Field> customFields = new ArrayList<>();
        private @Getter Type type;
        private @Getter boolean embed;
        private @Getter boolean closable;
        // Data
        private @Getter String customTitle;
        private @Getter String content;
        private @Getter SelectMenu.Builder selectMenuBuilder;

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

        public Builder setOverrideEmbed(EmbedBuilder embedBuilder) {
            this.customEmbedBuilder = embedBuilder;
            this.type = Type.CUSTOM;
            return this;
        }

        public Builder setCustomTitle(String customTitle) {
            this.customTitle = customTitle;
            return this;
        }

        public Builder addCustomField(MessageEmbed.Field field) {
            customFields.add(field);
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

        public Builder setSelectMenuBuilder(SelectMenu.Builder selectMenuBuilder) {
            this.selectMenuBuilder = selectMenuBuilder;
            return this;
        }

        public Builder generateSelectMenuBuilder(String placeholder) {
            this.selectMenuBuilder = SelectMenu.create(Integer.toString(new Random().nextInt())).setPlaceholder(placeholder);
            return this;
        }

        public Builder addInteraction(Interaction interaction, Consumer<GroupedInteractionEvent> onInteracted) {
            interactiveMessage.addInteraction(interaction, onInteracted);
            return this;
        }

        private void prepareMessage() {
            MessageEditBuilder messageEditBuilder = new MessageEditBuilder();

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

                    if (customTitle != null) {
                        embedBuilder.setTitle(customTitle);
                    }

                    int fields = embedBuilder.getFields().size();
                    for (MessageEmbed.Field field : customFields) {
                        if (fields == 25) {
                            break;
                        }

                        embedBuilder.addField(field);
                        fields++;
                    }

                    messageEditBuilder.setEmbeds(embedBuilder.build());
                } else {
                    switch (type) {
                        case ERROR:
                            messageEditBuilder.setContent(error(content));
                            break;
                        case WARNING:
                            messageEditBuilder.setContent(warning(content));
                            break;
                        case INFORMATION:
                            messageEditBuilder.setContent(information(content));
                            break;
                        case SUCCESS:
                            messageEditBuilder.setContent(success(content));
                            break;
                    }
                }
            } else {
                if (customColor != null) {
                    customEmbedBuilder.setColor(customColor);
                }

                messageEditBuilder.setEmbeds(customEmbedBuilder.build());
            }

            interactiveMessage.setMessageEditBuilder(messageEditBuilder);
            interactiveMessage.setSelectMenuBuilder(selectMenuBuilder);

            if (!interactionWhitelist.isEmpty()) {
                interactionWhitelist.forEach(interactiveMessage::addUserToWhitelist);
            }
        }

        public Message sendMessage(MessageChannelUnion messageChannelUnion) {
            prepareMessage();
            return interactiveMessage.sendMessage(messageChannelUnion).getMessage();
        }

        public Message editMessage(Message message) {
            prepareMessage();
            return interactiveMessage.editMessage(message).getMessage();
        }

        public Message sendMessage(InteractionHook interactionHook) {
            prepareMessage();
            return interactiveMessage.sendMessage(interactionHook).getMessage();
        }

        public Message editOriginal(InteractionHook interactionHook) {
            prepareMessage();
            return interactiveMessage.editOriginal(interactionHook).getMessage();
        }
    }
}
