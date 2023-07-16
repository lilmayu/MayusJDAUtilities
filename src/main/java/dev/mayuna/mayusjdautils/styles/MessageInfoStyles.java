package dev.mayuna.mayusjdautils.styles;

import dev.mayuna.mayusjdautils.MayusJDAUtilities;
import dev.mayuna.mayusjdautils.util.ExceptionUtils;
import dev.mayuna.mayusjdautils.util.SystemEmote;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class MessageInfoStyles implements Styles {

    protected final MayusJDAUtilities mayusJDAUtilities;

    protected EmbedBuilder defaultEmbedStyle = new EmbedBuilder().setFooter("Powered by Mayu's JDA Utilities")
                                                               .setTitle("Loading...")
                                                               .setDescription("Please wait.");

    protected @Getter boolean useUnicodeEmotes = true;

    public MessageInfoStyles(MayusJDAUtilities mayusJDAUtilities) {
        this.mayusJDAUtilities = mayusJDAUtilities;
    }

    /**
     * Gets default embed style
     *
     * @return EmbedBuilder
     */
    public @NotNull EmbedBuilder getDefaultEmbedStyle() {
        return defaultEmbedStyle;
    }

    /**
     * Sets default embed style
     *
     * @param embedBuilder Notnull embed builder
     */
    public void setDefaultEmbedStyle(@NotNull EmbedBuilder embedBuilder) {
        this.defaultEmbedStyle = embedBuilder;
    }

    /**
     * Gets default embed style with current timestamp
     *
     * @return EmbedBuilder
     */
    public @NotNull EmbedBuilder getDefaultEmbedStyleWithTimestamp() {
        return defaultEmbedStyle.setTimestamp(Instant.now());
    }

    /**
     * Determines if unicode emotes are used.<br> If you specify <code>true</code>, you must change emotes in {@link SystemEmote} to your specific
     * emotes.
     *
     * @param useUnicodeEmotes True / false
     */
    public void setUseUnicodeEmotes(boolean useUnicodeEmotes) {
        this.useUnicodeEmotes = useUnicodeEmotes;
    }

    /**
     * {@inheritDoc}
     *
     * @param styles Not-null styles
     */
    @Override
    public void copyFrom(@NotNull Styles styles) {
        if (!(styles instanceof MessageInfoStyles)) {
            return;
        }

        MessageInfoStyles otherMessageInfoStyles = (MessageInfoStyles) styles;

        defaultEmbedStyle = otherMessageInfoStyles.defaultEmbedStyle;
        useUnicodeEmotes = otherMessageInfoStyles.useUnicodeEmotes;
    }

    // Message Infos

    /**
     * Returns error message
     *
     * @param content Message content
     *
     * @return String
     */
    public String error(String content) {
        return (!useUnicodeEmotes ? SystemEmote.ERROR : "❌") + " | " + content;
    }

    /**
     * Returns warning message
     *
     * @param content Message content
     *
     * @return String
     */
    public String warning(String content) {
        return (!useUnicodeEmotes ? SystemEmote.WARNING : "❗") + " | " + content;
    }

    /**
     * Returns information message
     *
     * @param content Message content
     *
     * @return String
     */
    public String information(String content) {
        return (!useUnicodeEmotes ? SystemEmote.INFORMATION : "❔") + " | " + content;
    }

    /**
     * Returns success message
     *
     * @param content Message content
     *
     * @return String
     */
    public String success(String content) {
        return (!useUnicodeEmotes ? SystemEmote.SUCCESS : "✅") + " | " + content;
    }

    /**
     * Returns error embed
     *
     * @param title   Title
     * @param content Content
     * @param fields  Fields
     *
     * @return EmbedBuilder
     */
    public EmbedBuilder errorEmbed(String title, String content, MessageEmbed.Field... fields) {
        EmbedBuilder embedBuilder = quickEmbed(mayusJDAUtilities.getColorStyles().getError(), (!useUnicodeEmotes ? SystemEmote.ERROR : "❌") + " " + title, content);
        return addFields(embedBuilder, fields);
    }

    /**
     * Returns warning embed
     *
     * @param title   Title
     * @param content Content
     * @param fields  Fields
     *
     * @return EmbedBuilder
     */
    public EmbedBuilder warningEmbed(String title, String content, MessageEmbed.Field... fields) {
        EmbedBuilder embedBuilder = quickEmbed(mayusJDAUtilities.getColorStyles().getWarning(), (!useUnicodeEmotes ? SystemEmote.WARNING : "❗") + " " + title, content);
        return addFields(embedBuilder, fields);
    }

    /**
     * Returns information embed
     *
     * @param title   Title
     * @param content Content
     * @param fields  Fields
     *
     * @return EmbedBuilder
     */
    public EmbedBuilder informationEmbed(String title, String content, MessageEmbed.Field... fields) {
        EmbedBuilder embedBuilder = quickEmbed(mayusJDAUtilities.getColorStyles().getInformation(), (!useUnicodeEmotes ? SystemEmote.INFORMATION : "ℹ️") + " " + title, content);
        return addFields(embedBuilder, fields);
    }

    /**
     * Returns success embed
     *
     * @param title   Title
     * @param content Content
     * @param fields  Fields
     *
     * @return EmbedBuilder
     */
    public EmbedBuilder successEmbed(String title, String content, MessageEmbed.Field... fields) {
        EmbedBuilder embedBuilder = quickEmbed(mayusJDAUtilities.getColorStyles().getSuccess(), (!useUnicodeEmotes ? SystemEmote.SUCCESS : "✅") + " " + title, content);
        return addFields(embedBuilder, fields);
    }

    /**
     * Returns error embed
     *
     * @param content Content
     * @param fields  Fields
     *
     * @return EmbedBuilder
     */
    public EmbedBuilder errorEmbed(String content, MessageEmbed.Field... fields) {
        return errorEmbed(mayusJDAUtilities.getLanguageSettings().getOther().getError(), content, fields);
    }

    /**
     * Returns warning embed
     *
     * @param content Content
     * @param fields  Fields
     *
     * @return EmbedBuilder
     */
    public EmbedBuilder warningEmbed(String content, MessageEmbed.Field... fields) {
        return warningEmbed(mayusJDAUtilities.getLanguageSettings().getOther().getWarning(), content, fields);
    }

    /**
     * Returns information embed
     *
     * @param content Content
     * @param fields  Fields
     *
     * @return EmbedBuilder
     */
    public EmbedBuilder informationEmbed(String content, MessageEmbed.Field... fields) {
        return informationEmbed(mayusJDAUtilities.getLanguageSettings().getOther().getInformation(), content, fields);
    }

    /**
     * Returns success embed
     *
     * @param content Content
     * @param fields  Fields
     *
     * @return EmbedBuilder
     */
    public EmbedBuilder successEmbed(String content, MessageEmbed.Field... fields) {
        return successEmbed(mayusJDAUtilities.getLanguageSettings().getOther().getSuccess(), content, fields);
    }

    /**
     * Sends an exception message into the specified message channel with the specified throwable. Also, on the sent message, there will be added
     * exception.txt containing the exception stack.
     *
     * @param messageChannel Message channel
     * @param throwable      Throwable
     *
     * @return MessageCreateAction
     */
    public MessageCreateAction prepareExceptionMessageWithExceptionInFile(MessageChannel messageChannel, Throwable throwable) {
        MessageCreateBuilder messageCreateBuilder = new MessageCreateBuilder();

        messageCreateBuilder.setEmbeds(
                errorEmbed(mayusJDAUtilities.getLanguageSettings().getMessages().getExceptionOccurredMessage())
                        .addField(
                                mayusJDAUtilities.getLanguageSettings().getOther().getInformation(),
                                ExceptionUtils.formatExceptionInformationField(throwable),
                                false
                        ).build());

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);

        messageCreateBuilder.addFiles(FileUpload.fromData(stringWriter.toString().getBytes(StandardCharsets.UTF_8), "exception.txt"));
        return messageChannel.sendMessage(messageCreateBuilder.build());
    }

    // Protected methods

    protected EmbedBuilder quickEmbed(Color color, String title, String text) {
        return getDefaultEmbedStyleWithTimestamp().setColor(color).setTitle(title).setDescription(text);
    }

    protected EmbedBuilder addFields(EmbedBuilder embedBuilder, MessageEmbed.Field... fields) {
        if (fields != null) {
            for (MessageEmbed.Field field : fields) {
                embedBuilder.addField(field);
            }
        }

        return embedBuilder;
    }
}
