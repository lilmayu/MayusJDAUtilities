package lilmayu.mayusjdautilities.managed;

import com.google.gson.JsonObject;
import lilmayu.mayusjdautilities.exceptions.InvalidJsonException;
import lilmayu.mayusjsonutils.data.ISavable;
import lilmayu.mayusjsonutils.objects.MayuJson;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public abstract class MayuGuild implements ISavable {

    private @Getter Guild guild;
    private @Getter long guildId;

    public MayuGuild(long guildId) {
        setGuildId(guildId);
    }

    public MayuGuild(Guild guild) {
        setGuild(guild);
    }

    public void setGuild(Guild guild) {
        this.guild = guild;
        this.guildId = guild.getIdLong();
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
        this.guild = null;
    }

    public boolean isGuildValid() {
        return guildId != 0 && guild != null;
    }

    public boolean doesGuildExist(JDA jda) {
        return updateEntries(jda, true);
    }

    public boolean updateEntries(JDA jda) {
        return updateEntries(jda, false);
    }

    public boolean updateEntries(JDA jda, boolean force) {
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

    public void fromJsonObjectCore(JsonObject jsonObject) {
        try {
            MayuJson mayuJson = new MayuJson(jsonObject);

            this.guildId = mayuJson.getJsonObject().get("guildId").getAsLong();
        } catch (NullPointerException exception) {
            throw new InvalidJsonException("Invalid JSON for MayuGuild with ID " + this.guildId + " (this value may be 0 -> it does not exist in JSON)", jsonObject, exception);
        }
    }

    public JsonObject toJsonObjectCore() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("userId", this.guildId);

        return jsonObject;
    }

}
