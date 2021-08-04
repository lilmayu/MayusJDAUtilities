package lilmayu.mayusjdautilities.managed;

import com.google.gson.JsonObject;
import lilmayu.mayusjdautilities.exceptions.InvalidJsonException;
import lilmayu.mayusjsonutils.data.ISavable;
import lilmayu.mayusjsonutils.objects.MayuJson;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

public abstract class MayuUser implements ISavable {

    private @Getter User user;
    private @Getter long userId;

    public MayuUser(long userId) {
        setUserId(userId);
    }

    public MayuUser(User user) {
        setUser(user);
    }

    public MayuUser(JsonObject jsonObject) {
        fromJsonObject(jsonObject);
    }

    public void setUser(User user) {
        this.user = user;
        this.userId = user.getIdLong();
    }

    public void setUserId(long userId) {
        this.userId = userId;
        this.user = null;
    }

    public boolean isUserValid() {
        return userId != 0 && user != null;
    }

    public boolean doesUserExist(JDA jda) {
        return updateEntries(jda, true);
    }

    public boolean updateEntries(JDA jda) {
        return updateEntries(jda, false);
    }

    public boolean updateEntries(JDA jda, boolean force) {
        if (isUserValid()) {
            if (!force) {
                return true;
            }
        }

        if (userId == 0) {
            return false;
        }

        user = jda.retrieveUserById(userId).complete();

        return user != null;
    }

    public void fromJsonObjectCore(JsonObject jsonObject) {
        try {
            MayuJson mayuJson = new MayuJson(jsonObject);

            this.userId = mayuJson.getJsonObject().get("userId").getAsLong();
        } catch (NullPointerException exception) {
            throw new InvalidJsonException("Invalid JSON for MayuUser with ID " + this.userId + " (this value may be 0 -> it does not exist in JSON)", jsonObject, exception);
        }
    }

    public JsonObject toJsonObjectCore() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("userId", this.userId);

        return jsonObject;
    }
}
