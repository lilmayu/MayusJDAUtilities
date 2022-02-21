package dev.mayuna.mayusjdautils.managed;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import dev.mayuna.mayusjdautils.exceptions.InvalidGuildIDException;
import dev.mayuna.mayusjdautils.exceptions.InvalidTextChannelIDException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

/**
 * Managed text channel - Useful when working with text channels in guilds which can be saved into JSON<br>
 * Safe to use with {@link com.google.gson.Gson#toJson(Object)} if you use {@link com.google.gson.GsonBuilder} and {@link GsonBuilder#excludeFieldsWithoutExposeAnnotation()}
 */
public class ManagedTextChannel {

    // Raw data
    private @Getter @Setter @Expose String name;
    private @Getter @Expose @SerializedName("guildID") long rawGuildID;
    private @Getter @Expose @SerializedName(value = "textChannelID", alternate = {"messageChannelID"}) long rawTextChannelID; // messageChannelID for backwards compatibility

    // Discord data
    private @Getter @Expose(serialize = false, deserialize = false) Guild guild;
    private @Getter @Expose(serialize = false, deserialize = false) TextChannel textChannel;

    protected ManagedTextChannel(String name, Guild guild, TextChannel textChannel) {
        this.name = name;
        setGuild(guild);
        setTextChannel(textChannel);
    }

    protected ManagedTextChannel(String name, long rawGuildID, long rawTextChannelID) {
        this.name = name;
        this.rawGuildID = rawGuildID;
        this.rawTextChannelID = rawTextChannelID;
    }

    // Static creators

    /**
     * Creates {@link ManagedTextChannel} with specified raw IDs
     *
     * @param name             Name of {@link ManagedTextChannel}
     * @param rawGuildID       Raw Guild ID, must not be 0
     * @param rawTextChannelID Raw Text channel ID, must not be 0
     *
     * @return Non-null {@link ManagedTextChannel}
     *
     * @throws IllegalArgumentException if rawGuildID is zero or rawTextChannelID is zero
     */
    public static ManagedTextChannel create(String name, long rawGuildID, long rawTextChannelID) {
        if (rawGuildID <= 0) {
            throw new IllegalArgumentException("rawGuildID must not be 0!");
        }

        if (rawTextChannelID <= 0) {
            throw new IllegalArgumentException("rawTextChannelID must not be 0!");
        }

        return new ManagedTextChannel(name, rawGuildID, rawTextChannelID);
    }

    /**
     * Creates {@link ManagedTextChannel} with specified objects
     *
     * @param name        Name of {@link ManagedTextChannel}
     * @param guild       Non-null {@link Guild} object
     * @param textChannel Non-null {@link TextChannel} object
     *
     * @return Non-null {@link ManagedTextChannel}
     */
    public static ManagedTextChannel create(String name, @NonNull Guild guild, @NonNull TextChannel textChannel) {
        return new ManagedTextChannel(name, guild, textChannel);
    }

    // Others

    /**
     * Calls {@link #updateEntries(JDA, boolean, boolean)} with false, false values
     *
     * @param jda Non-null {@link JDA}
     *
     * @return True if entries are valid or if all entries were successfully updated
     */
    public boolean updateEntries(@NonNull JDA jda) {
        return updateEntries(jda, false, false);
    }

    /**
     * Updates all entries in {@link ManagedTextChannel}
     *
     * @param jda            Non-null {@link JDA}
     * @param force          Determines if this method should update entries even if all entries are valid
     * @param useExtraChecks Determines if this method should call more expensive and more thorough methods ({@link #isGuildValid(JDA)}, {@link #isTextChannelValid(JDA)}})
     *
     * @return True if entries are valid or if all entries were successfully updated
     */
    public boolean updateEntries(@NonNull JDA jda, boolean force, boolean useExtraChecks) {
        boolean valid;
        if (useExtraChecks) {
            valid = isGuildValid(jda) && isTextChannelValid(jda);
        } else {
            valid = isGuildValid() && isTextChannelValid();
        }
        if (valid) {
            if (!force) {
                return true;
            }
        }

        guild = jda.getGuildById(rawGuildID);
        if (guild == null) {
            throw new InvalidGuildIDException(rawGuildID);
        }

        textChannel = guild.getTextChannelById(rawTextChannelID);
        if (textChannel == null) {
            throw new InvalidTextChannelIDException(guild, rawTextChannelID);
        }

        return true;
    }

    /**
     * Checks if {@link ManagedTextChannel#guild} is not null and if {@link ManagedTextChannel#rawGuildID} equals to {@link ManagedTextChannel#guild}'s ID
     *
     * @return True if applies, false otherwise
     */
    public boolean isGuildValid() {
        if (guild != null) {
            return rawGuildID == guild.getIdLong();
        }

        return false;
    }

    /**
     * Calls {@link #isGuildValid()} and checks if JDA is connected to {@link ManagedTextChannel#guild}<br>
     * This method may take longer if your bot is on more guilds
     *
     * @param jda Non-null {@link JDA} object
     *
     * @return True if applies, false otherwise
     */
    public boolean isGuildValid(@NonNull JDA jda) {
        return isGuildValid() && jda.getGuilds().stream().anyMatch(jdaGuild -> jdaGuild.getIdLong() == rawGuildID);
    }

    /**
     * Checks if {@link ManagedTextChannel#textChannel} is not null and if {@link ManagedTextChannel#rawTextChannelID} equals to {@link ManagedTextChannel#textChannel}'s ID
     *
     * @return True if applies, false otherwise
     */
    public boolean isTextChannelValid() {
        if (textChannel != null) {
            return rawTextChannelID == textChannel.getIdLong();
        }

        return false;
    }

    /**
     * Calls {@link #isTextChannelValid()} and checks if JDA can find channel with {@link ManagedTextChannel#textChannel}'s ID<br>
     * This method may take longer if your bot is on more guilds
     *
     * @param jda Non-null {@link JDA} object
     *
     * @return True if applies, false otherwise
     */
    public boolean isTextChannelValid(@NonNull JDA jda) {
        return isTextChannelValid() && jda.getTextChannels().stream().anyMatch(jdaTextChannel -> jdaTextChannel.getIdLong() == rawTextChannelID);
    }

    // Setters

    /**
     * Sets specified value to {@link ManagedTextChannel#rawGuildID}.<br>
     * This automatically nulls {@link ManagedTextChannel#guild} and {@link ManagedTextChannel#textChannel}<br>
     * You will have to run {@link #updateEntries(JDA)} method to update them
     *
     * @param rawGuildID Raw Guild ID
     */
    public void setRawGuildID(long rawGuildID) {
        this.rawGuildID = rawGuildID;

        guild = null;
        textChannel = null;
    }

    /**
     * Sets specified value to {@link ManagedTextChannel#rawTextChannelID}.<br>
     * This automatically nulls {@link ManagedTextChannel#textChannel}<br>
     * You will have to run {@link #updateEntries(JDA)} method to update them
     *
     * @param rawTextChannelID Raw Text channel ID
     */
    public void setRawTextChannelID(long rawTextChannelID) {
        this.rawTextChannelID = rawTextChannelID;

        textChannel = null;
    }

    /**
     * Sets {@link Guild} object<br>
     * This automatically also sets {@link ManagedTextChannel#rawGuildID} to {@link Guild}'s ID
     *
     * @param guild Non-null {@link Guild}
     *
     * @return Non-null {@link ManagedTextChannel}
     */
    public ManagedTextChannel setGuild(@NonNull Guild guild) {
        this.guild = guild;
        this.rawGuildID = guild.getIdLong();
        return this;
    }

    /**
     * Sets {@link TextChannel} object<br>
     * This automatically also sets {@link ManagedTextChannel#rawTextChannelID} to {@link TextChannel}'s ID
     *
     * @param textChannel Non-null {@link TextChannel}
     *
     * @return Non-null {@link ManagedTextChannel}
     */
    public ManagedTextChannel setTextChannel(@NonNull TextChannel textChannel) {
        this.textChannel = textChannel;
        this.rawTextChannelID = textChannel.getIdLong();
        return this;
    }
}
