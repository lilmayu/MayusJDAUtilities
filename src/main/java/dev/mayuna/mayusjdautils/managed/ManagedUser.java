package dev.mayuna.mayusjdautils.managed;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

/**
 * Managed user - Useful when working with users which can be saved into JSON<br>
 * Safe to use with {@link com.google.gson.Gson#toJson(Object)} if you use {@link com.google.gson.GsonBuilder} and {@link GsonBuilder#excludeFieldsWithoutExposeAnnotation()}
 */
public class ManagedUser {

    // Raw data
    private @Getter @Setter @Expose String name;
    private @Getter @Expose @SerializedName("userID") long rawUserID;

    // Discord data
    private @Getter @Expose(serialize = false, deserialize = false) User user;

    protected ManagedUser(String name, User user) {
        this.name = name;
        setUser(user); // note: možná ditchnout #create() metody a prostě používat constructory?
    }

    protected ManagedUser(String name, long rawUserID) {
        this.name = name;
        this.rawUserID = rawUserID;
    }

    // Static creators

}
