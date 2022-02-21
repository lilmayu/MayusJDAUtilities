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

/**
 * Managed guild - Useful when working with guilds which can be saved into JSON<br>
 * Safe to use with {@link com.google.gson.Gson#toJson(Object)} if you use {@link com.google.gson.GsonBuilder} and {@link GsonBuilder#excludeFieldsWithoutExposeAnnotation()}
 */
public class ManagedGuild {

    // Raw data
    private @Getter @Setter @Expose String name;
    private @Getter @Expose @SerializedName("guildID") long rawGuildID;

    // Discord data
    private @Getter @Expose(serialize = false, deserialize = false) Guild guild;

    protected ManagedGuild(String name, Guild guild) {
        this.name = name;
        setGuild(guild);
    }

    protected ManagedGuild(String name, long rawGuildID) {
        this.name = name;
        this.rawGuildID = rawGuildID;
    }

    // Static creators

    /**
     * Creates {@link ManagedGuild} with specified raw IDs
     *
     * @param name       Name of {@link ManagedGuild}
     * @param rawGuildID Raw Guild ID, must not be 0
     *
     * @return Non-null {@link ManagedGuild}
     *
     * @throws IllegalArgumentException if rawGuildID is zero
     */
    public static ManagedGuild create(String name, long rawGuildID) {
        if (rawGuildID <= 0) {
            throw new IllegalArgumentException("rawGuildID must not be 0!");
        }

        return new ManagedGuild(name, rawGuildID);
    }

    /**
     * Creates {@link ManagedGuild} with specified objects
     *
     * @param name  Name of {@link ManagedGuild}
     * @param guild Non-null {@link Guild} object
     *
     * @return Non-null {@link ManagedGuild}
     */
    public static ManagedGuild create(String name, @NonNull Guild guild) {
        return new ManagedGuild(name, guild);
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
     * Updates all entries in {@link ManagedGuild}
     *
     * @param jda            Non-null {@link JDA}
     * @param force          Determines if this method should update entries even if all entries are valid
     * @param useExtraChecks Determines if this method should call more expensive and more thorough methods ({@link #isGuildValid(JDA)})
     *
     * @return True if entries are valid or if all entries were successfully updated
     */
    public boolean updateEntries(@NonNull JDA jda, boolean force, boolean useExtraChecks) {
        boolean valid;
        if (useExtraChecks) {
            valid = isGuildValid(jda);
        } else {
            valid = isGuildValid();
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

        return true;
    }

    /**
     * Checks if {@link ManagedGuild#guild} is not null and if {@link ManagedGuild#rawGuildID} equals to {@link ManagedGuild#guild}'s ID
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
     * Calls {@link #isGuildValid()} and checks if JDA is connected to {@link ManagedGuild#guild}<br>
     * This method may take longer if your bot is on more guilds
     *
     * @param jda Non-null {@link JDA} object
     *
     * @return True if applies, false otherwise
     */
    public boolean isGuildValid(@NonNull JDA jda) {
        return isGuildValid() && jda.getGuilds().stream().anyMatch(jdaGuild -> jdaGuild.getIdLong() == rawGuildID);
    }

    // Getters / Setters

    /**
     * Sets specified value to {@link ManagedGuild#rawGuildID}.<br>
     * This automatically nulls {@link ManagedGuild#guild}<br>
     * You will have to run {@link #updateEntries(JDA)} method to update them
     *
     * @param rawGuildID Raw Guild ID
     */
    public void setRawGuildID(long rawGuildID) {
        this.rawGuildID = rawGuildID;

        guild = null;
    }

    /**
     * Sets {@link Guild} object<br>
     * This automatically also sets {@link ManagedGuild#rawGuildID} to {@link Guild}'s ID
     *
     * @param guild Non-null {@link Guild}
     *
     * @return Non-null {@link ManagedGuild}
     */
    public ManagedGuild setGuild(@NonNull Guild guild) {
        this.guild = guild;
        this.rawGuildID = guild.getIdLong();
        return this;
    }
}
