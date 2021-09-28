package dev.mayuna.mayusjdautils.utils;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;

public class MessageInfo {

    public static boolean useSystemEmotes = false;

    // -- Basic Strings -- //

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

    // -- Basic Embed Builders -- //

    public static EmbedBuilder errorEmbed(String content) {
        return quickEmbed(Colors.getErrorColor(), useSystemEmotes ? SystemEmotes.ERROR + " Error" : "❌ Error", content);
    }

    public static EmbedBuilder warningEmbed(String content) {
        return quickEmbed(Colors.getWarningColor(), useSystemEmotes ? SystemEmotes.WARNING + " Warning" : "❗ Warning", content);
    }

    public static EmbedBuilder informationEmbed(String content) {
        return quickEmbed(Colors.getInformationColor(), useSystemEmotes ? SystemEmotes.INFORMATION + " Information" : "❔ Information", content);
    }

    public static EmbedBuilder successEmbed(String content) {
        return quickEmbed(Colors.getSuccessColor(), useSystemEmotes ? SystemEmotes.SUCCESS + " Success" : "✅ Success", content);
    }

    // -- Builder -- //

    public static class Builder {

        private @Getter String content;
        private @Getter Type type;
        private @Getter boolean embed;
        private @Getter boolean closable;
        private @Getter User closableBy;
        private @Getter long closeAfterMillis;

        public Builder create() {
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

        public Builder setClosableBy(User user) {
            this.closableBy = user;
            return this;
        }

        public Builder closeAfterMillis(long millis) {
            this.closeAfterMillis = millis;
            return this;
        }

        public Message send(MessageChannel messageChannel) {
            MessageBuilder messageBuilder = new MessageBuilder();

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

            // TODO: interaction

            return messageChannel.sendMessage(messageBuilder.build()).complete(); // todo: delete after
        }
    }

    // -- Other -- //

    // -- Colors -- //

    public static class Colors {

        private static @Getter @Setter Color defaultColor = new Color(0xFF0087);

        private static @Getter @Setter Color errorColor = new Color(0xE04642);
        private static @Getter @Setter Color informationColor = new Color(0x4C95D8);
        private static @Getter @Setter Color warningColor = new Color(0xEBC730);
        private static @Getter @Setter Color successColor = new Color(0x42D074);
    }

    public enum Type {
        ERROR, WARNING, INFORMATION, SUCCESS;
    }

    private static EmbedBuilder quickEmbed(Color color, String title, String text) {
        return DiscordUtils.getDefaultEmbed().setColor(color).setTitle(title).setDescription(text);
    }

}
