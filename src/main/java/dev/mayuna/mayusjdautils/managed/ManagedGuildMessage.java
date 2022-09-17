package dev.mayuna.mayusjdautils.managed;

import com.google.gson.annotations.SerializedName;
import dev.mayuna.mayusjdautils.exceptions.*;
import dev.mayuna.mayusjdautils.util.CallbackResult;
import dev.mayuna.mayusjdautils.util.DiscordUtils;
import dev.mayuna.mayusjdautils.util.RestActionMethod;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.util.function.Consumer;

/**
 * Managed guild message - Useful when working with messages in guilds which can be saved into JSON<br> Safe to use with
 * {@link com.google.gson.Gson#toJson(Object)}
 */
public class ManagedGuildMessage {

    // Raw data
    private @Getter @Setter String name;
    private @Getter @SerializedName("guildID") long rawGuildID;
    private @Getter @SerializedName(value = "textChannelID", alternate = {"messageChannelID"}) long rawTextChannelID; // messageChannelID for backwards compatibility
    private @Getter @SerializedName("messageID") long rawMessageID;

    // Discord data
    private transient @Getter Guild guild;
    private transient @Getter TextChannel textChannel;
    private transient @Getter Message message;

    /**
     * Constructs {@link ManagedGuildMessage} with specified objects
     *
     * @param name        Name of {@link ManagedGuildMessage}
     * @param guild       Non-null {@link Guild} object
     * @param textChannel Non-null {@link TextChannel} object
     * @param message     Nullable {@link TextChannel} object
     */
    public ManagedGuildMessage(String name, @NonNull Guild guild, @NonNull TextChannel textChannel, Message message) {
        this.name = name;
        setGuild(guild);
        setTextChannel(textChannel);
        setMessage(message);
    }

    /**
     * Constructs {@link ManagedGuildMessage} with specified raw IDs
     *
     * @param name             Name of {@link ManagedGuildMessage}
     * @param rawGuildID       Raw Guild ID, must not be 0
     * @param rawTextChannelID Raw Text channel ID, must not be 0
     * @param rawMessageID     Raw Message ID, can be 0
     *
     * @throws IllegalArgumentException if rawGuildID is zero or rawTextChannelID is zero
     */
    public ManagedGuildMessage(String name, long rawGuildID, long rawTextChannelID, long rawMessageID) {
        if (rawGuildID <= 0) {
            throw new IllegalArgumentException("rawGuildID must not be 0!");
        }

        if (rawTextChannelID <= 0) {
            throw new IllegalArgumentException("rawTextChannelID must not be 0!");
        }

        this.name = name;
        this.rawGuildID = rawGuildID;
        this.rawTextChannelID = rawTextChannelID;
        this.rawMessageID = rawMessageID;
    }

    // Others

    /**
     * Updates all entries in {@link ManagedGuildMessage} with supplied {@link JDA}, false, false, true, restActionMethod.COMPLETE, empty success
     * lambda, empty failure lambda
     *
     * @param jda Non-null {@link JDA}
     */
    public void updateEntries(@NonNull JDA jda) {
        updateEntries(jda, false, true, RestActionMethod.COMPLETE, success -> {
        }, failure -> {
        });
    }

    /**
     * Updates all entries in {@link ManagedGuildMessage} with supplied {@link ShardManager}, false, false, true, restActionMethod.COMPLETE, empty
     * success lambda, empty failure lambda
     *
     * @param shardManager Non-null {@link ShardManager}
     */
    public void updateEntries(@NonNull ShardManager shardManager) {
        updateEntriesEx(null, shardManager, false, true, RestActionMethod.COMPLETE, success -> {
        }, failure -> {
        });
    }

    /**
     * Updates all entries in {@link ManagedGuildMessage} with supplied {@link JDA}, false, false, supplied {@link RestActionMethod}, supplied success
     * lambda, supplied failure lambda
     *
     * @param jda              Non-null {@link JDA}
     * @param restActionMethod Determines which method should RestAction use (#queue() or #complete)
     * @param success          This consumer is called with non-null {@link CallbackResult} if entries were updated successfully
     * @param failure          This consumer is called with non-null {@link Exception} if updating entries failed. If there is Non-Discord Exception
     *                         (e.g. HTTP 500 error, SocketTimeoutException, etc.), {@link NonDiscordException} is supplied - In this case, you should
     *                         try calling this method again.
     */
    public void updateEntries(@NonNull JDA jda, @NonNull RestActionMethod restActionMethod, @NonNull Consumer<CallbackResult> success,
                              @NonNull Consumer<Exception> failure) {
        updateEntries(jda, false, true, restActionMethod, success, failure);
    }

    /**
     * Updates all entries in {@link ManagedGuildMessage} with supplied {@link ShardManager}, false, false, supplied {@link RestActionMethod},
     * supplied success lambda, supplied failure lambda
     *
     * @param shardManager     Non-null {@link ShardManager}
     * @param restActionMethod Determines which method should RestAction use (#queue() or #complete)
     * @param success          This consumer is called with non-null {@link CallbackResult} if entries were updated successfully
     * @param failure          This consumer is called with non-null {@link Exception} if updating entries failed. If there is Non-Discord Exception
     *                         (e.g. HTTP 500 error, SocketTimeoutException, etc.), {@link NonDiscordException} is supplied - In this case, you should
     *                         try calling this method again.
     */
    public void updateEntries(@NonNull ShardManager shardManager, @NonNull RestActionMethod restActionMethod, @NonNull Consumer<CallbackResult> success,
                              @NonNull Consumer<Exception> failure) {
        updateEntriesEx(null, shardManager, false, true, restActionMethod, success, failure);
    }

    /**
     * Updates all entries in {@link ManagedGuildMessage}
     *
     * @param jda                      Non-null {@link JDA}
     * @param force                    Determines if this method should update entries even if all entries are valid
     * @param sendNewMessageIfNotFound Determines if new message will be sent if current message cannot be found
     * @param restActionMethod         Determines which method should RestAction use (#queue() or #complete)
     * @param success                  This consumer is called with non-null {@link CallbackResult} if entries were updated successfully
     * @param failure                  This consumer is called with non-null {@link Exception} if updating entries failed. If there is Non-Discord
     *                                 Exception (e.g. HTTP 500 error, SocketTimeoutException, etc.), {@link NonDiscordException} is supplied - In
     *                                 this case, you should try calling this method again.
     */
    public void updateEntries(@NonNull JDA jda, boolean force, boolean sendNewMessageIfNotFound, @NonNull RestActionMethod restActionMethod,
                              @NonNull Consumer<CallbackResult> success, @NonNull Consumer<Exception> failure) {
        updateEntriesEx(jda, null, force, sendNewMessageIfNotFound, restActionMethod, success, failure);
    }

    /**
     * Updates all entries in {@link ManagedGuildMessage}
     *
     * @param shardManager             Non-null {@link ShardManager}
     * @param force                    Determines if this method should update entries even if all entries are valid
     * @param sendNewMessageIfNotFound Determines if new message will be sent if current message cannot be found
     * @param restActionMethod         Determines which method should RestAction use (#queue() or #complete)
     * @param success                  This consumer is called with non-null {@link CallbackResult} if entries were updated successfully
     * @param failure                  This consumer is called with non-null {@link Exception} if updating entries failed. If there is Non-Discord
     *                                 Exception (e.g. HTTP 500 error, SocketTimeoutException, etc.), {@link NonDiscordException} is supplied - In
     *                                 this case, you should try calling this method again.
     */
    public void updateEntries(@NonNull ShardManager shardManager, boolean force, boolean sendNewMessageIfNotFound, @NonNull RestActionMethod restActionMethod,
                              @NonNull Consumer<CallbackResult> success, @NonNull Consumer<Exception> failure) {
        updateEntriesEx(null, shardManager, force, sendNewMessageIfNotFound, restActionMethod, success, failure);
    }

    private void updateEntriesEx(JDA jda, ShardManager shardManager, boolean force, boolean sendNewMessageIfNotFound, @NonNull RestActionMethod restActionMethod,
                                 @NonNull Consumer<CallbackResult> success, @NonNull Consumer<Exception> failure) {
        Runnable sendNewMessageRunnable = () -> {
            switch (restActionMethod) {
                case QUEUE: {
                    textChannel.sendMessage(DiscordUtils.getDefaultMessageCreateBuilder().build()).queue(message -> {
                        setMessage(message);
                        success.accept(CallbackResult.SENT);
                    }, exception -> {
                        handleException(exception, failure, () -> {
                            failure.accept(new CannotSendNewMessageException(exception, guild, textChannel));
                        });
                    });
                    return;
                }
                case COMPLETE: {
                    try {
                        setMessage(textChannel.sendMessage(DiscordUtils.getDefaultMessageCreateBuilder().build()).complete());
                        success.accept(CallbackResult.SENT);
                    } catch (Exception exception) {
                        handleException(exception, failure, () -> {
                            failure.accept(new CannotSendNewMessageException(exception, guild, textChannel));
                        });
                    }
                    return;
                }
                default: {
                    failure.accept(new IllegalArgumentException("BUG! No rest action method: " + restActionMethod));
                    return;
                }
            }
        };

        boolean valid = isGuildValid() && isTextChannelValid() && isMessageValid();

        if (valid) {
            if (!force) {
                success.accept(CallbackResult.NOTHING);
                return;
            }
        }

        if (jda != null) {
            guild = jda.getGuildById(rawGuildID);
        } else {
            guild = shardManager.getGuildById(rawGuildID);
        }

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
                try {
                    textChannel.retrieveMessageById(rawMessageID).queue(message -> {
                        setMessage(message);
                        success.accept(CallbackResult.RETRIEVED);
                    }, exception -> {
                        handleException(exception, failure, () -> {
                            if (sendNewMessageIfNotFound) {
                                sendNewMessageRunnable.run();
                            } else {
                                failure.accept(new InvalidMessageIDException(exception, guild, textChannel, rawMessageID));
                            }
                        });
                    });
                } catch (Exception exception) {
                    handleException(exception, failure, () -> {
                        failure.accept(new InvalidMessageIDException(exception, guild, textChannel, rawMessageID));
                    });
                }
                return;
            }
            case COMPLETE: {
                try {
                    setMessage(textChannel.retrieveMessageById(rawMessageID).complete());
                    success.accept(CallbackResult.RETRIEVED);
                } catch (Exception exception) {
                    handleException(exception, failure, () -> {
                        if (sendNewMessageIfNotFound) {
                            sendNewMessageRunnable.run();
                        } else {
                            failure.accept(new InvalidMessageIDException(exception, guild, textChannel, rawMessageID));
                        }
                    });
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
     * Calls {@link #sendOrEditMessage(MessageEditBuilder, RestActionMethod, Consumer, Consumer)} with arguments: null, provided message, false,
     * RestActionMethod.COMPLETE, empty lambda, empty lambda<br> Ignores if message was not successfully sent, if not found.
     *
     * @param messageEditBuilder Non-null {@link MessageEditBuilder}
     */
    public void sendOrEditMessage(@NonNull MessageEditBuilder messageEditBuilder) {
        sendOrEditMessage(messageEditBuilder, RestActionMethod.COMPLETE, success -> {
        }, failure -> {
        });
    }

    /**
     * Edits current message in {@link ManagedGuildMessage}, if failed, tries to send new message into current
     * {@link ManagedGuildMessage#textChannel}
     *
     * @param messageEditBuilder Non-null {@link MessageEditBuilder}
     * @param restActionMethod   Determines which method should RestAction use (#queue() or #complete)
     * @param success            This consumer is called with non-null {@link CallbackResult} if message was successfully edited or sent
     * @param failure            This consumer is called with non-null {@link Exception} if editing or sending failed. These exceptions are possible:
     *                           {@link CannotSendNewMessageException} and {@link InvalidMessageIDException}. If there is Non-Discord Exception (e.g.
     *                           HTTP 500 error, SocketTimeoutException, etc.), {@link NonDiscordException} is supplied - In this case, you should try
     *                           calling this method again.
     */
    public void sendOrEditMessage(@NonNull MessageEditBuilder messageEditBuilder, @NonNull RestActionMethod restActionMethod, @NonNull Consumer<CallbackResult> success,
                                  @NonNull Consumer<Exception> failure) {
        Consumer<Message> sendNewMessageConsumer = (messageToSend) -> {
            boolean textChannelValid = isTextChannelValid();

            if (textChannelValid) {
                try {
                    RestAction<Message> messageRestAction = textChannel.sendMessage(MessageCreateBuilder.fromEditData(messageEditBuilder.build())
                                                                                                        .build());

                    switch (restActionMethod) {
                        case QUEUE: {
                            messageRestAction.queue(sentMessage -> {
                                setMessage(sentMessage);
                                success.accept(CallbackResult.SENT);
                            }, exception -> {
                                handleException(exception, failure, () -> {
                                    failure.accept(new CannotSendNewMessageException(exception, guild, textChannel));
                                });
                            });
                            return;
                        }
                        case COMPLETE: {
                            try {
                                setMessage(messageRestAction.complete());
                                success.accept(CallbackResult.SENT);
                            } catch (Exception exception) {
                                handleException(exception, failure, () -> {
                                    failure.accept(new CannotSendNewMessageException(exception, guild, textChannel));
                                });
                            }
                            return;
                        }
                        default: {
                            failure.accept(new IllegalArgumentException("BUG! No rest action method: " + restActionMethod));
                            return;
                        }
                    }
                } catch (Exception exception) {
                    handleException(exception, failure, () -> {
                        failure.accept(new CannotSendNewMessageException(exception, guild, textChannel));
                    });
                }
            } else {
                failure.accept(new InvalidTextChannelIDException(guild, rawTextChannelID));
            }
        };

        boolean messageValid = isMessageValid();

        if (messageValid) {
            RestAction<Message> messageRestAction = this.message.editMessage(messageEditBuilder.build());

            switch (restActionMethod) {
                case QUEUE: {
                    messageRestAction.queue(editedMessage -> {
                        success.accept(CallbackResult.EDITED);
                    }, exception -> {
                        handleException(exception, failure, () -> {
                            sendNewMessageConsumer.accept(message);
                        });
                    });
                    return;
                }
                case COMPLETE: {
                    try {
                        messageRestAction.complete();
                        success.accept(CallbackResult.EDITED);
                    } catch (Exception exception) {
                        handleException(exception, failure, () -> {
                            sendNewMessageConsumer.accept(message);
                        });
                    }
                    return;
                }
                default: {
                    failure.accept(new IllegalArgumentException("BUG! No rest action method: " + restActionMethod));
                    return;
                }
            }
        } else {
            sendNewMessageConsumer.accept(message);
        }
    }

    /**
     * Checks if {@link ManagedGuildMessage#guild} is not null and if {@link ManagedGuildMessage#rawGuildID} equals to
     * {@link ManagedGuildMessage#guild}'s ID
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
     * Checks if {@link ManagedGuildMessage#textChannel} is not null and if {@link ManagedGuildMessage#rawTextChannelID} equals to
     * {@link ManagedGuildMessage#textChannel}'s ID
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
     * Checks if {@link ManagedGuildMessage#message} is not null and if {@link ManagedGuildMessage#rawMessageID} equals to
     * {@link ManagedGuildMessage#message}'s ID
     *
     * @return True if applies, false otherwise
     */
    public boolean isMessageValid() {
        if (message != null) {
            return rawMessageID == message.getIdLong();
        }

        return false;
    }

    // Getters / Setters

    /**
     * Sets specified value to {@link ManagedGuildMessage#rawGuildID}.<br> This automatically nulls {@link ManagedGuildMessage#guild},
     * {@link ManagedGuildMessage#textChannel} and {@link ManagedGuildMessage#message}<br> You will have to run {@link #updateEntries(JDA)} method to
     * update them
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
     * Sets specified value to {@link ManagedGuildMessage#rawTextChannelID}.<br> This automatically nulls {@link ManagedGuildMessage#textChannel} and
     * {@link ManagedGuildMessage#message}<br> You will have to run {@link #updateEntries(JDA)} method to update them
     *
     * @param rawTextChannelID Raw Message channel ID
     */
    public void setRawTextChannelID(long rawTextChannelID) {
        this.rawTextChannelID = rawTextChannelID;

        textChannel = null;
        message = null;
    }

    /**
     * Sets specified value to {@link ManagedGuildMessage#rawMessageID}.<br> This automatically nulls {@link ManagedGuildMessage#message}<br> You will
     * have to run {@link #updateEntries(JDA)} method to update it
     *
     * @param rawMessageID Raw Message ID
     */
    public void setRawMessageID(long rawMessageID) {
        this.rawMessageID = rawMessageID;

        message = null;
    }

    /**
     * Sets {@link Guild} object<br> This automatically also sets {@link ManagedGuildMessage#rawGuildID} to {@link Guild}'s ID
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
     * Sets {@link TextChannel} object<br> This automatically also sets {@link ManagedGuildMessage#rawTextChannelID} to {@link TextChannel}'s ID
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
     * Sets {@link Message} object<br> This automatically also sets {@link ManagedGuildMessage#rawMessageID} to {@link Message}'s ID if not null,
     * otherwise sets {@link ManagedGuildMessage#rawMessageID} to 0
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

    private void handleException(Throwable throwable, Consumer<Exception> failure, Runnable onDiscordException) {
        if (DiscordUtils.isDiscordException(throwable)) {
            onDiscordException.run();
        } else {
            failure.accept(new NonDiscordException(throwable));
        }
    }
}
