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
import net.dv8tion.jda.api.sharding.ShardManager;

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

    /**
     * Constructs {@link ManagedTextChannel} with specified objects
     *
     * @param name        Name of {@link ManagedTextChannel}
     * @param guild       Non-null {@link Guild} object
     * @param textChannel Non-null {@link TextChannel} object
     */
    public ManagedTextChannel(String name, @NonNull Guild guild, @NonNull TextChannel textChannel) {
        this.name = name;
        setGuild(guild);
        setTextChannel(textChannel);
    }

    /**
     * Constructs {@link ManagedTextChannel} with specified raw IDs
     *
     * @param name             Name of {@link ManagedTextChannel}
     * @param rawGuildID       Raw Guild ID, must not be 0
     * @param rawTextChannelID Raw Text channel ID, must not be 0
     *
     * @throws IllegalArgumentException if rawGuildID is zero or rawTextChannelID is zero
     */
    public ManagedTextChannel(String name, long rawGuildID, long rawTextChannelID) {
        if (rawGuildID <= 0) {
            throw new IllegalArgumentException("rawGuildID must not be 0!");
        }

        if (rawTextChannelID <= 0) {
            throw new IllegalArgumentException("rawTextChannelID must not be 0!");
        }

        this.name = name;
        this.rawGuildID = rawGuildID;
        this.rawTextChannelID = rawTextChannelID;
    }

    // Others

    /**
     * Calls {@link #updateEntries(JDA, boolean)} with false value
     *
     * @param jda Non-null {@link JDA}
     *
     * @return True if entries are valid or if all entries were successfully updated
     */
    public boolean updateEntries(@NonNull JDA jda) {
        return updateEntries(jda, false);
    }

    /**
     * Calls {@link #updateEntries(ShardManager, boolean)} with false value
     *
     * @param shardManager Non-null {@link ShardManager}
     *
     * @return True if entries are valid or if all entries were successfully updated
     */
    public boolean updateEntries(@NonNull ShardManager shardManager) {
        return updateEntries(shardManager, false);
    }

    /**
     * Updates all entries in {@link ManagedTextChannel}
     *
     * @param jda   Non-null {@link JDA}
     * @param force Determines if this method should update entries even if all entries are valid
     *
     * @return True if entries are valid or if all entries were successfully updated
     */
    public boolean updateEntries(@NonNull JDA jda, boolean force) {
        return updateEntriesEx(jda, null, force);
    }

    /**
     * Updates all entries in {@link ManagedTextChannel}
     *
     * @param shardManager Non-null {@link ShardManager}
     * @param force        Determines if this method should update entries even if all entries are valid
     *
     * @return True if entries are valid or if all entries were successfully updated
     */
    public boolean updateEntries(@NonNull ShardManager shardManager, boolean force) {
        return updateEntriesEx(null, shardManager, force);
    }

    private boolean updateEntriesEx(JDA jda, ShardManager shardManager, boolean force) {
        boolean valid = isGuildValid() && isTextChannelValid();

        if (valid) {
            if (!force) {
                return true;
            }
        }

        if (jda != null) {
            guild = jda.getGuildById(rawGuildID);
        } else {
            guild = shardManager.getGuildById(rawGuildID);
        }

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
