package dev.mayuna.mayusjdautils.managed;

import com.google.gson.annotations.SerializedName;
import dev.mayuna.mayusjdautils.exceptions.CannotRetrieveUserException;
import dev.mayuna.mayusjdautils.exceptions.NonDiscordException;
import dev.mayuna.mayusjdautils.util.CallbackResult;
import dev.mayuna.mayusjdautils.util.DiscordUtils;
import dev.mayuna.mayusjdautils.util.RestActionMethod;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.function.Consumer;

/**
 * Managed user - Useful when working with users which can be saved into JSON<br>
 * Safe to use with {@link com.google.gson.Gson#toJson(Object)}
 */
public class ManagedUser {

    // Raw data
    private @Getter @Setter String name;
    private @Getter @SerializedName("userID") long rawUserId;

    // Discord data
    private transient @Getter User user;

    /**
     * Constructs {@link ManagedUser} with specified objects
     *
     * @param name Name of {@link ManagedUser}
     * @param user Non-null {@link User} object
     */
    public ManagedUser(String name, User user) {
        this.name = name;
        setUser(user);
    }

    /**
     * Constructs {@link ManagedUser} with specified raw IDs
     *
     * @param name      Name of {@link ManagedUser}
     * @param rawUserId Raw User ID, must not be 0
     *
     * @throws IllegalArgumentException if rawUserID is zero
     */
    public ManagedUser(String name, long rawUserId) {
        this.name = name;
        this.rawUserId = rawUserId;
    }

    // Others

    /**
     * Calls {@link #updateEntries(JDA, boolean, RestActionMethod, Consumer, Consumer)} with supplied {@link JDA}, false, restActionMethod.COMPLETE,
     * empty success lambda, empty failure lambda
     *
     * @param jda Non-null {@link JDA}
     */
    public void updateEntries(@NonNull JDA jda) {
        updateEntries(jda, false, RestActionMethod.COMPLETE, success -> {}, failure -> {});
    }

    /**
     * Calls {@link #updateEntries(ShardManager, boolean, RestActionMethod, Consumer, Consumer)} with supplied {@link ShardManager}, false, restActionMethod.COMPLETE,
     * empty success lambda, empty failure lambda
     *
     * @param shardManager Non-null {@link ShardManager}
     */
    public void updateEntries(@NonNull ShardManager shardManager) {
        updateEntries(shardManager, false, RestActionMethod.COMPLETE, success -> {}, failure -> {});
    }

    /**
     * Updates all entries in {@link ManagedUser}
     *
     * @param jda              Non-null {@link JDA}
     * @param force            Determines if this method should update entries even if all entries are valid
     * @param restActionMethod Determines which method should RestAction use (#queue() or #complete)
     * @param success          This consumer is called with non-null {@link CallbackResult} if user was successfully retrieved
     * @param failure          This consumer is called with non-null {@link Exception} if editing or sending failed. These exceptions are possible:
     *                         {@link CannotRetrieveUserException}. If there is Non-Discord Exception (e.g. HTTP 500 error, SocketTimeoutException, etc.),
     *                         {@link NonDiscordException} is supplied - In this case, you should try calling this method again.
     */
    public void updateEntries(@NonNull JDA jda, boolean force, @NonNull RestActionMethod restActionMethod, @NonNull Consumer<CallbackResult> success,
            @NonNull Consumer<Exception> failure) {
        updateEntriesEx(jda, null, force, restActionMethod, success, failure);
    }

    /**
     * Updates all entries in {@link ManagedUser}
     *
     * @param shardManager     Non-null {@link ShardManager}
     * @param force            Determines if this method should update entries even if all entries are valid
     * @param restActionMethod Determines which method should RestAction use (#queue() or #complete)
     * @param success          This consumer is called with non-null {@link CallbackResult} if user was successfully retrieved
     * @param failure          This consumer is called with non-null {@link Exception} if editing or sending failed. These exceptions are possible:
     *                         {@link CannotRetrieveUserException}. If there is Non-Discord Exception (e.g. HTTP 500 error, SocketTimeoutException, etc.),
     *                         {@link NonDiscordException} is supplied - In this case, you should try calling this method again.
     */
    public void updateEntries(@NonNull ShardManager shardManager, boolean force, @NonNull RestActionMethod restActionMethod, @NonNull Consumer<CallbackResult> success,
            @NonNull Consumer<Exception> failure) {
        updateEntriesEx(null, shardManager, force, restActionMethod, success, failure);
    }

    private void updateEntriesEx(JDA jda, ShardManager shardManager, boolean force, @NonNull RestActionMethod restActionMethod, @NonNull Consumer<CallbackResult> success,
            @NonNull Consumer<Exception> failure) {
        boolean valid = isUserValid();

        if (valid) {
            if (!force) {
                success.accept(CallbackResult.NOTHING);
                return;
            }
        }

        switch (restActionMethod) {
            case QUEUE: {
                Consumer<User> userConsumer = user -> {
                    setUser(user);
                    success.accept(CallbackResult.RETRIEVED);
                };
                Consumer<? super Throwable> exceptionConsumer = exception -> {
                    handleException(exception, failure, () -> {
                        failure.accept(new CannotRetrieveUserException(exception, rawUserId));
                    });
                };

                if (jda != null) {
                    jda.retrieveUserById(rawUserId).queue(userConsumer, exceptionConsumer);
                } else {
                    shardManager.retrieveUserById(rawUserId).queue(userConsumer, exceptionConsumer);
                }
                return;
            }
            case COMPLETE: {
                try {
                    if (jda != null) {
                        setUser(jda.retrieveUserById(rawUserId).complete());
                    } else {
                        setUser(shardManager.retrieveUserById(rawUserId).complete());
                    }
                    success.accept(CallbackResult.RETRIEVED);
                } catch (Exception exception) {
                    handleException(exception, failure, () -> {
                        failure.accept(new CannotRetrieveUserException(exception, rawUserId));
                    });
                }
                return;
            }
        }
    }

    /**
     * Checks if {@link ManagedUser#user} is not null and if {@link ManagedUser#rawUserId} equals to {@link ManagedUser#user}'s ID
     *
     * @return True if applies, false otherwise
     */
    public boolean isUserValid() {
        if (user != null) {
            return rawUserId == user.getIdLong();
        }

        return false;
    }

    // Setters

    /**
     * Sets specified value to {@link ManagedUser#rawUserId}.<br>
     * This automatically nulls {@link ManagedUser#user}<br>
     * You will have to run {@link #updateEntries(JDA)} method to update them
     *
     * @param rawUserId Raw User ID
     *
     * @return Non-null {@link ManagedUser}
     */
    public ManagedUser setRawUserId(long rawUserId) {
        this.rawUserId = rawUserId;
        user = null;

        return this;
    }

    /**
     * Sets {@link User} object<br>
     * This automatically also sets {@link ManagedUser#rawUserId} to {@link User}'s ID
     *
     * @param user Non-null {@link User}
     *
     * @return Non-null {@link ManagedUser}
     */
    public ManagedUser setUser(@NonNull User user) {
        this.rawUserId = user.getIdLong();
        this.user = user;

        return this;
    }

    private void handleException(Throwable throwable, Consumer<Exception> failure, Runnable onDiscordException) {
        if (DiscordUtils.isDiscordException(throwable)) {
            onDiscordException.run();
        } else {
            failure.accept(new NonDiscordException(throwable));
        }
    }
}
