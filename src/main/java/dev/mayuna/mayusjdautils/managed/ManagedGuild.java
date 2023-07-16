package dev.mayuna.mayusjdautils.managed;

import com.google.gson.annotations.SerializedName;
import dev.mayuna.mayusjdautils.exceptions.InvalidGuildIDException;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.io.Serializable;

/**
 * Managed guild - Useful when working with guilds which can be saved into JSON<br>
 * Safe to use with {@link com.google.gson.Gson#toJson(Object)}
 */
public class ManagedGuild implements Serializable {

    // Raw data
    private @Getter @Setter String name;
    private @Getter @SerializedName("guildID") long rawGuildId;

    // Discord data
    private transient @Getter Guild guild;

    /**
     * Constructs {@link ManagedGuild} with specified objects
     *
     * @param name  Name of {@link ManagedGuild}
     * @param guild Non-null {@link Guild} object
     */
    public ManagedGuild(String name, @NonNull Guild guild) {
        this.name = name;
        setGuild(guild);
    }

    /**
     * Constructs {@link ManagedGuild} with specified raw IDs
     *
     * @param name       Name of {@link ManagedGuild}
     * @param rawGuildId Raw Guild ID, must not be 0
     *
     * @throws IllegalArgumentException if rawGuildID is zero
     */
    public ManagedGuild(String name, long rawGuildId) {
        if (rawGuildId <= 0) {
            throw new IllegalArgumentException("rawGuildID must not be 0!");
        }

        this.name = name;
        this.rawGuildId = rawGuildId;
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
     * Calls {@link #updateEntries(ShardManager, boolean)} with false values
     *
     * @param shardManager Non-null {@link ShardManager}
     *
     * @return True if entries are valid or if all entries were successfully updated
     */
    public boolean updateEntries(@NonNull ShardManager shardManager) {
        return updateEntries(shardManager, false);
    }

    /**
     * Updates all entries in {@link ManagedGuild}
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
     * Updates all entries in {@link ManagedGuild}
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
        boolean valid = isGuildValid();

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

        return true;
    }

    /**
     * Checks if {@link ManagedGuild#guild} is not null and if {@link ManagedGuild#rawGuildId} equals to {@link ManagedGuild#guild}'s ID
     *
     * @return True if applies, false otherwise
     */
    public boolean isGuildValid() {
        if (guild != null) {
            return rawGuildId == guild.getIdLong();
        }

        return false;
    }

    // Setters

    /**
     * Sets specified value to {@link ManagedGuild#rawGuildId}.<br>
     * This automatically nulls {@link ManagedGuild#guild}<br>
     * You will have to run {@link #updateEntries(JDA)} method to update them
     *
     * @param rawGuildId Raw Guild ID
     *
     * @return Non-null {@link ManagedGuild}
     */
    public ManagedGuild setRawGuildId(long rawGuildId) {
        this.rawGuildId = rawGuildId;
        guild = null;

        return this;
    }

    /**
     * Sets {@link Guild} object<br>
     * This automatically also sets {@link ManagedGuild#rawGuildId} to {@link Guild}'s ID
     *
     * @param guild Non-null {@link Guild}
     *
     * @return Non-null {@link ManagedGuild}
     */
    public ManagedGuild setGuild(@NonNull Guild guild) {
        this.guild = guild;
        this.rawGuildId = guild.getIdLong();

        return this;
    }
}
