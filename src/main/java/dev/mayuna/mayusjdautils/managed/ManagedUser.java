package dev.mayuna.mayusjdautils.managed;

import com.google.gson.JsonObject;
import dev.mayuna.mayusjdautils.exceptions.InvalidJsonException;
import dev.mayuna.mayusjsonutils.data.Savable;
import dev.mayuna.mayusjsonutils.objects.MayuJson;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;

public abstract class ManagedUser implements Savable {

    // Data
    private @Getter User user;
    private @Getter long userId;

    /**
     * Creates {@link ManagedUser} from User ID
     *
     * @param userId Valid User ID
     */
    public ManagedUser(long userId) {
        setUserId(userId);
    }

    /**
     * Creates {@link ManagedUser} from {@link User} object
     *
     * @param user Non-null {@link User} object
     */
    public ManagedUser(@NonNull User user) {
        setUser(user);
    }

    /**
     * Creates {@link ManagedUser} from {@link JsonObject} object <br>
     * **Note**: You need to call {@link #fromJsonObject(JsonObject)} again after construction if you extend class with this {@link ManagedUser} class!
     *
     * @param jsonObject Non-null {@link JsonObject} object
     */
    public ManagedUser(@NonNull JsonObject jsonObject) {
        fromJsonObject(jsonObject);
    }

    /**
     * Sets {@link ManagedUser}'s User and User ID to specified User
     *
     * @param user Non-null {@link User} object
     */
    public void setUser(@NonNull User user) {
        this.user = user;
        this.userId = user.getIdLong();
    }

    /**
     * Sets {@link ManagedUser}'s User ID to specified User ID. Sets current User to null - you can update it with {@link #updateEntries(JDA)}
     *
     * @param userId Valid User ID
     */
    public void setUserId(long userId) {
        this.userId = userId;
        this.user = null;
    }

    /**
     * Checks whenever UserID is valid and User is not null
     *
     * @return true if both of them are valid (non-zero and non-null)
     */
    public boolean isUserValid() {
        return userId != 0 && user != null;
    }

    /**
     * Checks if user exists. This method calls {@link #updateEntries(JDA, boolean)} with force flag set to true
     *
     * @param jda Non-null {@link JDA} object
     * @return true if user exists
     */
    public boolean doesUserExist(@NonNull JDA jda) {
        return updateEntries(jda, true);
    }

    /**
     * Updates entries. This method calls {@link #updateEntries(JDA, boolean)} with force flag set to false
     *
     * @param jda Non-null {@link JDA} object
     * @return true if User is valid or if JDA could get non-null User from Discord
     */
    public boolean updateEntries(@NonNull JDA jda) {
        return updateEntries(jda, false);
    }

    /**
     * Updates entries
     *
     * @param jda Non-null {@link JDA} object
     * @param force True if it should ignore return value from {@link #isUserValid()} and update entries even if User is valid
     * @return true if User is valid or if JDA could get non-null User from Discord
     */
    public boolean updateEntries(@NonNull JDA jda, boolean force) {
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

    /**
     * Parses {@link ManagedUser} from {@link JsonObject} <br>
     * Specified JSON must contain "userId" element
     *
     * @param jsonObject Non-null {@link JsonObject}
     */
    public void fromJsonObjectCore(@NonNull JsonObject jsonObject) {
        try {
            MayuJson mayuJson = new MayuJson(jsonObject);

            this.userId = mayuJson.getJsonObject().get("userId").getAsLong();
        } catch (NullPointerException exception) {
            throw new InvalidJsonException("Invalid JSON for MayuUser with ID " + this.userId + " (this value may be 0 -> it does not exist in JSON)", jsonObject, exception);
        }
    }

    /**
     * Returns {@link JsonObject} with saved entries
     *
     * @return Non-null {@link JsonObject}
     */
    public JsonObject toJsonObjectCore() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("userId", this.userId);

        return jsonObject;
    }
}
