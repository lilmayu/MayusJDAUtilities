package dev.mayuna.mayusjdautils.managed;

import com.google.gson.JsonObject;
import dev.mayuna.mayusjdautils.exceptions.InvalidJsonException;
import dev.mayuna.mayusjsonutils.data.Savable;
import dev.mayuna.mayusjsonutils.objects.MayuJson;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

public abstract class ManagedGuild implements Savable {

    // Data
    private @Getter Guild guild;
    private @Getter long guildId;

    /**
     * Creates {@link ManagedGuild} from Guild ID
     *
     * @param guildId Valid User ID
     */
    public ManagedGuild(long guildId) {
        setGuildId(guildId);
    }

    /**
     * Creates {@link ManagedGuild} from {@link Guild} object
     *
     * @param guild Non-null {@link Guild} object
     */
    public ManagedGuild(@NonNull Guild guild) {
        setGuild(guild);
    }

    /**
     * Creates {@link ManagedGuild} from {@link JsonObject} object <br>
     * **Note**: You need to call {@link #fromJsonObject(JsonObject)} again after construction if you extend class with this {@link ManagedGuild} class!
     *
     * @param jsonObject Non-null {@link JsonObject} object
     */
    public ManagedGuild(@NonNull JsonObject jsonObject) {
        fromJsonObject(jsonObject);
    }

    /**
     * Sets {@link ManagedGuild}'s ManagedGuild and Guild ID to specified Guild
     *
     * @param guild Non-null {@link Guild} object
     */
    public void setGuild(@NonNull Guild guild) {
        this.guild = guild;
        this.guildId = guild.getIdLong();
    }

    /**
     * Sets {@link ManagedGuild}'s Guild ID to specified Guild ID. Sets current Guild to null - you can update it with {@link #updateEntries(JDA)}
     *
     * @param guildId Valid Guild ID
     */
    public void setGuildId(long guildId) {
        this.guildId = guildId;
        this.guild = null;
    }

    /**
     * Checks whenever GuildID is valid and Guild is not null
     *
     * @return true if both of them are valid (non-zero and non-null)
     */
    public boolean isGuildValid() {
        return guildId != 0 && guild != null;
    }

    /**
     * Checks if guild exists. This method calls {@link #updateEntries(JDA, boolean)} with force flag set to true
     *
     * @param jda Non-null {@link JDA} object
     *
     * @return true if guild exists
     */
    public boolean doesGuildExist(@NonNull JDA jda) {
        return updateEntries(jda, true);
    }

    /**
     * Updates entries. This method calls {@link #updateEntries(JDA, boolean)} with force flag set to false
     *
     * @param jda Non-null {@link JDA} object
     *
     * @return true if Guild is valid or if JDA could get non-null Guild from Discord
     */
    public boolean updateEntries(@NonNull JDA jda) {
        return updateEntries(jda, false);
    }

    /**
     * Updates entries
     *
     * @param jda   Non-null {@link JDA} object
     * @param force True if it should ignore return value from {@link #isGuildValid()} and update entries even if Guild is valid
     *
     * @return true if Guild is valid or if JDA could get non-null Guild from Discord
     */
    public boolean updateEntries(@NonNull JDA jda, boolean force) {
        if (isGuildValid()) {
            if (!force) {
                return true;
            }
        }

        if (guildId == 0) {
            return false;
        }

        guild = jda.getGuildById(guildId);

        return guild != null;
    }

    /**
     * Parses {@link ManagedGuild} from {@link JsonObject} <br>
     * Specified JSON must contain "guildId" element
     *
     * @param jsonObject Non-null {@link JsonObject}
     */
    public void fromJsonObjectCore(@NonNull JsonObject jsonObject) {
        try {
            MayuJson mayuJson = new MayuJson(jsonObject);

            this.guildId = mayuJson.getJsonObject().get("guildId").getAsLong();
        } catch (NullPointerException exception) {
            throw new InvalidJsonException("Invalid JSON for MayuGuild with ID " + this.guildId + " (this value may be 0 -> it does not exist in JSON)", jsonObject, exception);
        }
    }

    /**
     * Returns {@link JsonObject} with saved entries
     *
     * @return Non-null {@link JsonObject}
     */
    public JsonObject toJsonObjectCore() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("guildId", this.guildId);

        return jsonObject;
    }
}
