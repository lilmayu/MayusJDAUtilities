package dev.mayuna.mayusjdautils.managed;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import dev.mayuna.mayusjdautils.exceptions.CannotSendNewMessageException;
import dev.mayuna.mayusjdautils.exceptions.InvalidGuildIDException;
import dev.mayuna.mayusjdautils.exceptions.InvalidMessageIDException;
import dev.mayuna.mayusjdautils.exceptions.InvalidTextChannelIDException;
import dev.mayuna.mayusjdautils.utils.CallbackResult;
import dev.mayuna.mayusjdautils.utils.DiscordUtils;
import dev.mayuna.mayusjdautils.utils.RestActionMethod;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;

import java.util.function.Consumer;

/**
 * Managed guild message - Useful when working with messages in guilds which can be saved into JSON<br>
 * Safe to use with {@link com.google.gson.Gson#toJson(Object)} if you use {@link com.google.gson.GsonBuilder} and {@link GsonBuilder#excludeFieldsWithoutExposeAnnotation()}
 */
public class ManagedGuildMessage {

    // Raw data
    private @Getter @Setter @Expose String name;
    private @Getter @Expose @SerializedName("guildID") long rawGuildID;
    private @Getter @Expose @SerializedName(value = "textChannelID", alternate = {"messageChannelID"}) long rawTextChannelID; // messageChannelID for backwards compatibility
    private @Getter @Expose @SerializedName("messageID") long rawMessageID;

    // Discord data
    private @Getter @Expose(serialize = false, deserialize = false) Guild guild;
    private @Getter @Expose(serialize = false, deserialize = false) TextChannel textChannel;
    private @Getter @Expose(serialize = false, deserialize = false) Message message;

    protected ManagedGuildMessage(String name, Guild guild, TextChannel textChannel, Message message) {
        this.name = name;
        setGuild(guild);
        setTextChannel(textChannel);
        setMessage(message);
    }

    protected ManagedGuildMessage(String name, long rawGuildID, long rawTextChannelID, long rawMessageID) {
        this.name = name;
        this.rawGuildID = rawGuildID;
        this.rawTextChannelID = rawTextChannelID;
        this.rawMessageID = rawMessageID;
    }

    // Static creators

    /**
     * Creates {@link ManagedGuildMessage} with specified raw IDs
     *
     * @param name             Name of {@link ManagedGuildMessage}
     * @param rawGuildID       Raw Guild ID, must not be 0
     * @param rawTextChannelID Raw Text channel ID, must not be 0
     * @param rawMessageID     Raw Message ID, can be 0
     *
     * @return Non-null {@link ManagedGuildMessage}
     *
     * @throws IllegalArgumentException if rawGuildID is zero or rawTextChannelID is zero
     */
    public static ManagedGuildMessage create(String name, long rawGuildID, long rawTextChannelID, long rawMessageID) {
        if (rawGuildID <= 0) {
            throw new IllegalArgumentException("rawGuildID must not be 0!");
        }

        if (rawTextChannelID <= 0) {
            throw new IllegalArgumentException("rawTextChannelID must not be 0!");
        }

        return new ManagedGuildMessage(name, rawGuildID, rawTextChannelID, rawMessageID);
    }

    /**
     * Creates {@link ManagedGuildMessage} with specified objects
     *
     * @param name        Name of {@link ManagedGuildMessage}
     * @param guild       Non-null {@link Guild} object
     * @param textChannel Non-null {@link TextChannel} object
     * @param message     Nullable {@link TextChannel} object
     *
     * @return Non-null {@link ManagedGuildMessage}
     */
    public static ManagedGuildMessage create(String name, @NonNull Guild guild, @NonNull TextChannel textChannel, Message message) {
        return new ManagedGuildMessage(name, guild, textChannel, message);
    }

    // Others

    /**
     * Calls {@link #updateEntries(JDA, boolean, boolean, boolean)} with false, false, true values
     *
     * @param jda Non-null {@link JDA}
     *
     * @return True if entries are valid or if all entries were successfully updated
     */
    public boolean updateEntries(@NonNull JDA jda) {
        return updateEntries(jda, false, false, true);
    }

    /**
     * Updates all entries in {@link ManagedGuildMessage}
     *
     * @param jda                      Non-null {@link JDA}
     * @param force                    Determines if this method should update entries even if all entries are valid
     * @param useExtraChecks           Determines if this method should call more expensive and more thorough methods ({@link #isGuildValid(JDA)}, {@link #isTextChannelValid(JDA)}, {@link #isMessageValid(JDA)})
     * @param sendNewMessageIfNotFound Determines if new message will be sent if current message cannot be found
     *
     * @return True if entries are valid or if all entries were successfully updated
     */
    public void updateEntries(@NonNull JDA jda, boolean force, boolean useExtraChecks, boolean sendNewMessageIfNotFound, @NonNull RestActionMethod restActionMethod,
            @NonNull Consumer<CallbackResult> success, @NonNull Consumer<Exception> failure) {

        Runnable sendNewMessageRunnable = () -> {
            switch (restActionMethod) {
                case QUEUE: {
                    textChannel.sendMessage(DiscordUtils.getDefaultMessageBuilder().build()).queue(message -> {
                        setMessage(message);
                        success.accept(CallbackResult.SENT);
                    }, exception -> {
                        failure.accept(new CannotSendNewMessageException(exception, guild, textChannel));
                    });
                    return;
                }
                case COMPLETE: {
                    try {
                        setMessage(textChannel.sendMessage(DiscordUtils.getDefaultMessageBuilder().build()).complete());
                        success.accept(CallbackResult.SENT);
                    } catch (Exception exception) {
                        failure.accept(new CannotSendNewMessageException(exception, guild, textChannel));
                    }
                    return;
                }
                default: {
                    failure.accept(new IllegalArgumentException("BUG! No rest action method: " + restActionMethod));
                    return;
                }
            }
        };

        boolean valid;
        if (useExtraChecks) {
            valid = isGuildValid(jda) && isTextChannelValid(jda) && isMessageValid(jda);
        } else {
            valid = isGuildValid() && isTextChannelValid() && isMessageValid();
        }
        if (valid) {
            if (!force) {
                success.accept(CallbackResult.NOTHING);
                return;
            }
        }

        guild = jda.getGuildById(rawGuildID);
        if (guild == null) {
            failure.accept(new InvalidGuildIDException(rawGuildID));
            return;
        }

        textChannel = guild.getTextChannelById(rawTextChannelID);
        if (textChannel == null) {
            failure.accept(new InvalidTextChannelIDException(guild, rawTextChannelID));
            return;
        }

        switch (restActionMethod) {
            case QUEUE: {
                textChannel.retrieveMessageById(rawMessageID).queue(message -> {
                    setMessage(message);
                    success.accept(CallbackResult.RETRIEVED);
                }, exception -> {
                    if (sendNewMessageIfNotFound) {
                        sendNewMessageRunnable.run();
                    } else {
                        failure.accept(new InvalidMessageIDException(exception, guild, textChannel, rawMessageID));
                    }
                });
                return;
            }
            case COMPLETE: {
                try {
                    setMessage(textChannel.retrieveMessageById(rawMessageID).complete());
                    success.accept(CallbackResult.RETRIEVED);
                } catch (Exception exception) {
                    if (sendNewMessageIfNotFound) {
                        sendNewMessageRunnable.run();
                    } else {
                        failure.accept(new InvalidMessageIDException(exception, guild, textChannel, rawMessageID));
                    }
                }
                return;
            }
            default: {
                failure.accept(new IllegalArgumentException("BUG! No rest action method: " + restActionMethod));
                return;
            }
        }
    }

    /**
     * Calls {@link #sendOrEditMessage(Message, RestActionMethod, Consumer, Consumer)} with arguments: null, provided message, false, RestActionMethod.COMPLETE, empty lambda, empty lambda<br>
     * Ignores if message was not successfully sent, if not found.
     *
     * @param message Non-null {@link Message} (can be from {@link MessageBuilder}
     */
    public void sendOrEditMessage(@NonNull Message message) {
        sendOrEditMessage(null, message, false, RestActionMethod.COMPLETE, success -> {}, failure -> {});
    }

    /**
     * Calls {@link #sendOrEditMessage(JDA, Message, boolean, RestActionMethod, Consumer, Consumer)} with arguments: null, provided message, false, provided restActionMethod, provided success callback, provided failure callback
     *
     * @param message          Non-null {@link Message} (can be from {@link MessageBuilder}
     * @param restActionMethod Determines which method should RestAction use (#queue() or #complete)
     * @param success          This consumer is called with non-null {@link CallbackResult} if message was successfully edited or sent
     * @param failure          This consumer is called with non-null {@link Exception} if editing or sending failed. These exceptions are possible: {@link CannotSendNewMessageException} and {@link InvalidMessageIDException}
     */
    public void sendOrEditMessage(@NonNull Message message, @NonNull RestActionMethod restActionMethod, @NonNull Consumer<CallbackResult> success,
            @NonNull Consumer<Exception> failure) {
        sendOrEditMessage(null, message, false, restActionMethod, success, failure);
    }

    /**
     * Edits current message in {@link ManagedGuildMessage}, if failed, tries to send new message into current {@link ManagedGuildMessage#textChannel}
     *
     * @param jda              Nullable {@link JDA} if useExtraChecks argument is set false, otherwise must be non-null
     * @param message          Non-null {@link Message} (can be from {@link MessageBuilder}
     * @param useExtraChecks   Determines if this method should call more expensive and more thorough methods ({@link #isGuildValid(JDA)}, {@link #isMessageValid(JDA)})
     * @param restActionMethod Determines which method should RestAction use (#queue() or #complete)
     * @param success          This consumer is called with non-null {@link CallbackResult} if message was successfully edited or sent
     * @param failure          This consumer is called with non-null {@link Exception} if editing or sending failed. These exceptions are possible: {@link CannotSendNewMessageException} and {@link InvalidMessageIDException}
     */
    public void sendOrEditMessage(JDA jda, @NonNull Message message, boolean useExtraChecks, @NonNull RestActionMethod restActionMethod, @NonNull Consumer<CallbackResult> success,
            @NonNull Consumer<Exception> failure) {
        Consumer<Message> sendNewMessageConsumer = (messageToSend) -> {
            boolean textChannelValid;
            if (useExtraChecks) {
                textChannelValid = isTextChannelValid(jda);
            } else {
                textChannelValid = isTextChannelValid();
            }

            if (textChannelValid) {
                try {
                    RestAction<Message> messageRestAction = textChannel.sendMessage(messageToSend);

                    switch (restActionMethod) {
                        case QUEUE: {
                            messageRestAction.queue(sentMessage -> {
                                setMessage(sentMessage);
                                success.accept(CallbackResult.SENT);
                            }, exception -> {
                                failure.accept(new CannotSendNewMessageException(exception, guild, textChannel));
                            });
                            return;
                        }
                        case COMPLETE: {
                            try {
                                setMessage(messageRestAction.complete());
                                success.accept(CallbackResult.SENT);
                            } catch (Exception exception) {
                                failure.accept(new CannotSendNewMessageException(exception, guild, textChannel));
                            }
                            return;
                        }
                        default: {
                            failure.accept(new IllegalArgumentException("BUG! No rest action method: " + restActionMethod));
                            return;
                        }
                    }
                } catch (Exception exception) {
                    failure.accept(new CannotSendNewMessageException(exception, guild, textChannel));
                }
            }

            failure.accept(new InvalidTextChannelIDException(guild, rawTextChannelID));
        };

        boolean messageValid;
        if (useExtraChecks) {
            messageValid = isMessageValid(jda);
        } else {
            messageValid = isMessageValid();
        }

        if (messageValid) {
            RestAction<Message> messageRestAction = this.message.editMessage(message);

            switch (restActionMethod) {
                case QUEUE: {
                    messageRestAction.queue(editedMessage -> {
                        success.accept(CallbackResult.EDITED);
                    }, exception -> {
                        sendNewMessageConsumer.accept(message);
                    });
                    return;
                }
                case COMPLETE: {
                    try {
                        messageRestAction.complete();
                        success.accept(CallbackResult.EDITED);
                    } catch (Exception ignored) {
                        sendNewMessageConsumer.accept(message);
                    }
                    return;
                }
                default: {
                    failure.accept(new IllegalArgumentException("BUG! No rest action method: " + restActionMethod));
                    return;
                }
            }
        }
    }

    /**
     * Checks if {@link ManagedGuildMessage#guild} is not null and if {@link ManagedGuildMessage#rawGuildID} equals to {@link ManagedGuildMessage#guild}'s ID
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
     * Calls {@link #isGuildValid()} and checks if JDA is connected to {@link ManagedGuildMessage#guild}<br>
     * This method may take longer if your bot is on more guilds
     *
     * @param jda Non-null {@link JDA} object
     *
     * @return True if applies, false otherwise
     */
    public boolean isGuildValid(@NonNull JDA jda) {
        return isGuildValid() && jda.getGuilds().stream().anyMatch(jdaGuild -> jdaGuild.getIdLong() == rawGuildID);
    }

    /**
     * Checks if {@link ManagedGuildMessage#textChannel} is not null and if {@link ManagedGuildMessage#rawTextChannelID} equals to {@link ManagedGuildMessage#textChannel}'s ID
     *
     * @return True if applies, false otherwise
     */
    public boolean isTextChannelValid() {
        if (textChannel != null) {
            return rawTextChannelID == textChannel.getIdLong();
        }

        return false;
    }

    /**
     * Calls {@link #isTextChannelValid()} and checks if JDA can find channel with {@link ManagedGuildMessage#textChannel}'s ID<br>
     * This method may take longer if your bot is on more guilds
     *
     * @param jda Non-null {@link JDA} object
     *
     * @return True if applies, false otherwise
     */
    public boolean isTextChannelValid(@NonNull JDA jda) {
        return isTextChannelValid() && jda.getTextChannels().stream().anyMatch(jdaTextChannel -> jdaTextChannel.getIdLong() == rawTextChannelID);
    }

    /**
     * Checks if {@link ManagedGuildMessage#message} is not null and if {@link ManagedGuildMessage#rawMessageID} equals to {@link ManagedGuildMessage#message}'s ID
     *
     * @return True if applies, false otherwise
     */
    public boolean isMessageValid() {
        if (message != null) {
            return rawMessageID == message.getIdLong();
        }

        return false;
    }

    /**
     * Calls {@link #isMessageValid()} and {@link #isTextChannelValid()} and checks if JDA can retrieve message in {@link ManagedGuildMessage#textChannel} with {@link ManagedGuildMessage#message}'s ID<br>
     *
     * @param jda Non-null {@link JDA} object
     *
     * @return True if applies, false otherwise
     */
    public boolean isMessageValid(@NonNull JDA jda) {
        try {
            return isMessageValid() && isTextChannelValid() && textChannel.retrieveMessageById(rawMessageID).complete() != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    // Getters / Setters

    /**
     * Sets specified value to {@link ManagedGuildMessage#rawGuildID}.<br>
     * This automatically nulls {@link ManagedGuildMessage#guild}, {@link ManagedGuildMessage#textChannel} and {@link ManagedGuildMessage#message}<br>
     * You will have to run {@link #updateEntries(JDA)} method to update them
     *
     * @param rawGuildID Raw Guild ID
     */
    public void setRawGuildID(long rawGuildID) {
        this.rawGuildID = rawGuildID;

        guild = null;
        textChannel = null;
        message = null;
    }

    /**
     * Sets specified value to {@link ManagedGuildMessage#rawTextChannelID}.<br>
     * This automatically nulls {@link ManagedGuildMessage#textChannel} and {@link ManagedGuildMessage#message}<br>
     * You will have to run {@link #updateEntries(JDA)} method to update them
     *
     * @param rawTextChannelID Raw Message channel ID
     */
    public void setRawTextChannelID(long rawTextChannelID) {
        this.rawTextChannelID = rawTextChannelID;

        textChannel = null;
        message = null;
    }

    /**
     * Sets specified value to {@link ManagedGuildMessage#rawMessageID}.<br>
     * This automatically nulls {@link ManagedGuildMessage#message}<br>
     * You will have to run {@link #updateEntries(JDA)} method to update it
     *
     * @param rawMessageID Raw Message ID
     */
    public void setRawMessageID(long rawMessageID) {
        this.rawMessageID = rawMessageID;

        message = null;
    }

    /**
     * Sets {@link Guild} object<br>
     * This automatically also sets {@link ManagedGuildMessage#rawGuildID} to {@link Guild}'s ID
     *
     * @param guild Non-null {@link Guild}
     *
     * @return Non-null {@link ManagedGuildMessage}
     */
    public ManagedGuildMessage setGuild(@NonNull Guild guild) {
        this.guild = guild;
        this.rawGuildID = guild.getIdLong();
        return this;
    }

    /**
     * Sets {@link TextChannel} object<br>
     * This automatically also sets {@link ManagedGuildMessage#rawTextChannelID} to {@link TextChannel}'s ID
     *
     * @param textChannel Non-null {@link TextChannel}
     *
     * @return Non-null {@link ManagedGuildMessage}
     */
    public ManagedGuildMessage setTextChannel(@NonNull TextChannel textChannel) {
        this.textChannel = textChannel;
        this.rawTextChannelID = textChannel.getIdLong();
        return this;
    }

    /**
     * Sets {@link Message} object<br>
     * This automatically also sets {@link ManagedGuildMessage#rawMessageID} to {@link Message}'s ID if not null, otherwise sets {@link ManagedGuildMessage#rawMessageID} to 0
     *
     * @param message Nullable {@link Message}
     *
     * @return Non-null {@link ManagedGuildMessage}
     */
    public ManagedGuildMessage setMessage(Message message) {
        if (message == null) {
            this.message = null;
            this.rawMessageID = 0;
        } else {
            this.message = message;
            this.rawMessageID = message.getIdLong();
        }

        return this;
    }
}
