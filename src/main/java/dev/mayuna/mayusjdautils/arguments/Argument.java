package dev.mayuna.mayusjdautils.arguments;

import dev.mayuna.mayusjdautils.utils.DiscordUtils;
import dev.mayuna.mayuslibrary.utils.NumberUtils;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

public class Argument {

    // Data
    private final @Getter String value;

    /**
     * Creates {@link Argument} with specified non-null value
     *
     * @param value Argument's value
     */
    public Argument(@NonNull String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Gets argument's value as {@link Number}
     *
     * @return {@link Number} if parsable, otherwise null
     */
    public Number getValueAsNumber() {
        return NumberUtils.parseNumber(value);
    }

    /**
     * Gets argument's value as {@link User}
     *
     * @param jda Non-null {@link JDA} object
     *
     * @return Possibly-null {@link User} if exists
     */
    public User getValueAsUser(@NonNull JDA jda) {
        return DiscordUtils.isUserMention(value) ? jda.retrieveUserById(DiscordUtils.getMentionID(value)).complete() : null;
    }

    /**
     * Gets argument's value as {@link Guild}
     *
     * @param guild Non-null {@link Guild} object
     *
     * @return Possibly-null  {@link Role} if exists
     */
    public Role getValueAsRole(Guild guild) {
        return DiscordUtils.isRoleMention(value) ? guild.getRoleById(DiscordUtils.getMentionID(value)) : null;
    }

    /**
     * Gets argument's value as {@link TextChannel}
     *
     * @param guild Non-null {@link Guild} object
     *
     * @return Possibly-null {@link TextChannel} if exists
     */
    public TextChannel getValueAsTextChannel(Guild guild) {
        return DiscordUtils.isChannelMention(value) ? guild.getTextChannelById(DiscordUtils.getMentionID(value)) : null;
    }

    /**
     * Gets argument's value as {@link Emote}
     *
     * @param guild Non-null {@link Guild} object
     *
     * @return Possibly-null {@link Emote} if exists
     */
    public Emote getValueAsEmote(Guild guild) {
        return DiscordUtils.isEmoteMention(value) ? guild.retrieveEmoteById(DiscordUtils.getMentionID(value)).complete() : null;
    }
}
