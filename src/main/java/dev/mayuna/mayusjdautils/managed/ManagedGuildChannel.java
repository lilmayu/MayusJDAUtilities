package dev.mayuna.mayusjdautils.managed;

import com.google.gson.annotations.SerializedName;
import dev.mayuna.mayusjdautils.exceptions.InvalidGuildChannelIDException;
import dev.mayuna.mayusjdautils.exceptions.InvalidGuildIDException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.sharding.ShardManager;

/**
 * Managed text channel - Useful when working with text channels in guilds which can be saved into JSON<br>
 * Safe to use with {@link com.google.gson.Gson#toJson(Object)}
 */
public class ManagedGuildChannel {

    // Raw data
    private @Getter @Setter String name;
    private @Getter @SerializedName("guildID") long rawGuildId;
    private @Getter @SerializedName(value = "guildChannelID") long rawGuildChannelId;

    // Discord data
    private transient @Getter Guild guild;
    private transient @Getter GuildChannel guildChannel;

    /**
     * Constructs {@link ManagedGuildChannel} with specified objects
     *
     * @param name        Name of {@link ManagedGuildChannel}
     * @param guild       Non-null {@link Guild} object
     * @param GuildChannel Non-null {@link GuildChannel} object
     */
    public ManagedGuildChannel(String name, @NonNull Guild guild, @NonNull GuildChannel GuildChannel) {
        this.name = name;
        setGuild(guild);
        setGuildChannel(GuildChannel);
    }

    /**
     * Constructs {@link ManagedGuildChannel} with specified raw IDs
     *
     * @param name             Name of {@link ManagedGuildChannel}
     * @param rawGuildId       Raw Guild ID, must not be 0
     * @param rawGuildChannelId Raw Guild channel ID, must not be 0
     *
     * @throws IllegalArgumentException if rawGuildID is zero or rawGuildChannelID is zero
     */
    public ManagedGuildChannel(String name, long rawGuildId, long rawGuildChannelId) {
        if (rawGuildId <= 0) {
            throw new IllegalArgumentException("rawGuildID must not be 0!");
        }

        if (rawGuildChannelId <= 0) {
            throw new IllegalArgumentException("rawGuildChannelID must not be 0!");
        }

        this.name = name;
        this.rawGuildId = rawGuildId;
        this.rawGuildChannelId = rawGuildChannelId;
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
     * Updates all entries in {@link ManagedGuildChannel}
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
     * Updates all entries in {@link ManagedGuildChannel}
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
        boolean valid = isGuildValid() && isGuildChannelValid();

        if (valid) {
            if (!force) {
                return true;
            }
        }

        if (jda != null) {
            guild = jda.getGuildById(rawGuildId);
        } else {
            guild = shardManager.getGuildById(rawGuildId);
        }

        if (guild == null) {
            throw new InvalidGuildIDException(rawGuildId);
        }

        guildChannel = guild.getGuildChannelById(rawGuildChannelId);
        if (guildChannel == null) {
            throw new InvalidGuildChannelIDException(guild, rawGuildChannelId);
        }

        return true;
    }

    /**
     * Checks if {@link ManagedGuildChannel#guild} is not null and if {@link ManagedGuildChannel#rawGuildId} equals to {@link ManagedGuildChannel#guild}'s ID
     *
     * @return True if applies, false otherwise
     */
    public boolean isGuildValid() {
        if (guild != null) {
            return rawGuildId == guild.getIdLong();
        }

        return false;
    }

    /**
     * Checks if {@link ManagedGuildChannel#guildChannel} is not null and if {@link ManagedGuildChannel#rawGuildChannelId} equals to {@link ManagedGuildChannel#guildChannel}'s ID
     *
     * @return True if applies, false otherwise
     */
    public boolean isGuildChannelValid() {
        if (guildChannel != null) {
            return rawGuildChannelId == guildChannel.getIdLong();
        }

        return false;
    }

    // Setters

    /**
     * Sets specified value to {@link ManagedGuildChannel#rawGuildId}.<br>
     * This automatically nulls {@link ManagedGuildChannel#guild} and {@link ManagedGuildChannel#guildChannel}<br>
     * You will have to run {@link #updateEntries(JDA)} method to update them
     *
     * @param rawGuildId Raw Guild ID
     */
    public void setRawGuildId(long rawGuildId) {
        this.rawGuildId = rawGuildId;

        guild = null;
        guildChannel = null;
    }

    /**
     * Sets specified value to {@link ManagedGuildChannel#rawGuildChannelId}.<br>
     * This automatically nulls {@link ManagedGuildChannel#guildChannel}<br>
     * You will have to run {@link #updateEntries(JDA)} method to update them
     *
     * @param rawGuildChannelId Raw Text channel ID
     */
    public void setRawGuildChannelId(long rawGuildChannelId) {
        this.rawGuildChannelId = rawGuildChannelId;

        guildChannel = null;
    }

    /**
     * Sets {@link Guild} object<br>
     * This automatically also sets {@link ManagedGuildChannel#rawGuildId} to {@link Guild}'s ID
     *
     * @param guild Non-null {@link Guild}
     *
     * @return Non-null {@link ManagedGuildChannel}
     */
    public ManagedGuildChannel setGuild(@NonNull Guild guild) {
        this.guild = guild;
        this.rawGuildId = guild.getIdLong();
        return this;
    }

    /**
     * Sets {@link GuildChannel} object<br>
     * This automatically also sets {@link ManagedGuildChannel#rawGuildChannelId} to {@link GuildChannel}'s ID
     *
     * @param GuildChannel Non-null {@link GuildChannel}
     *
     * @return Non-null {@link ManagedGuildChannel}
     */
    public ManagedGuildChannel setGuildChannel(@NonNull GuildChannel GuildChannel) {
        this.guildChannel = GuildChannel;
        this.rawGuildChannelId = GuildChannel.getIdLong();
        return this;
    }
}
