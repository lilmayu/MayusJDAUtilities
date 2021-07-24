package lilmayu.mayusjdautilities.managed;

import com.google.gson.JsonObject;
import lilmayu.mayusjdautilities.exceptions.FailedToGetTextChannelGuildException;
import lilmayu.mayusjdautilities.exceptions.InvalidGuildIDException;
import lilmayu.mayusjdautilities.exceptions.InvalidJsonException;
import lilmayu.mayusjdautilities.exceptions.InvalidMessageIDException;
import lilmayu.mayusjdautilities.utils.DiscordUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class ManagedMessage {

    // Name
    private final @Getter String name;

    // IDs
    private @Getter @Setter long guildID;
    private @Getter @Setter long messageChannelID;
    private @Getter @Setter long messageID;

    // Discord Data
    private @Getter Guild guild;
    private @Getter Message message;
    private @Getter MessageChannel messageChannel;

    // Checks
    private @Getter boolean guildValid = false;
    private @Getter boolean messageChannelValid = false;
    private @Getter boolean messageValid = false;
    private @Getter boolean resolved = false;

    // Others
    private @Getter @Setter MessageBuilder defaultMessage = new MessageBuilder().setEmbed(DiscordUtils.getDefaultEmbed().setTimestamp(null).build());

    // -- Constructs -- //

    public ManagedMessage(String name) {
        this.name = name;
    }

    public ManagedMessage(String name, long guildID, long messageChannelID, long messageID) {
        this.name = name;
        this.guildID = guildID;
        this.messageChannelID = messageChannelID;
        this.messageID = messageID;
    }

    public ManagedMessage(String name, @NonNull Guild guild, @NonNull MessageChannel messageChannel) {
        this.name = name;

        setGuild(guild);
        setMessageChannel(messageChannel);
    }

    public ManagedMessage(String name, @NonNull Guild guild, @NonNull MessageChannel messageChannel, Message message) {
        this.name = name;

        setGuild(guild);
        setMessageChannel(messageChannel);
        setMessage(message);
    }

    // -- JSON stuff -- //

    public static ManagedMessage fromJsonObject(JsonObject jsonObject) {
        String name = null;
        long guildID, messageChannelID, messageID;
        try {
            name = jsonObject.get("name").getAsString();
            guildID = jsonObject.get("guildID").getAsLong();
            messageChannelID = jsonObject.get("messageChannelID").getAsLong();
            messageID = jsonObject.get("messageID").getAsLong();
            return new ManagedMessage(name, guildID, messageChannelID, messageID);
        } catch (NullPointerException exception) {
            throw new InvalidJsonException("Invalid json for ManagedMessage with name: " + name, jsonObject);
        }
    }

    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", name);
        jsonObject.addProperty("guildID", guildID);
        jsonObject.addProperty("messageChannelID", messageChannelID);
        jsonObject.addProperty("messageID", messageID);

        return jsonObject;
    }

    // -- Main Logic -- //

    public boolean updateEntries(JDA jda) {
        return updateEntries(jda, false);
    }

    public boolean updateEntries(JDA jda, boolean force) {
        if (areEntriesValid()) {
            if (!force) {
                return true;
            }
        }

        guild = jda.getGuildById(guildID);
        if (guild == null) {
            throw new InvalidGuildIDException(guildID);
        }
        guildValid = true;

        messageChannel = guild.getTextChannelById(messageChannelID);
        if (messageChannel == null) {
            throw new FailedToGetTextChannelGuildException(guild, messageChannelID);
        }
        messageChannelValid = true;

        try {
            message = messageChannel.retrieveMessageById(messageID).complete();
        } catch (ErrorResponseException exception) {
            if (defaultMessage != null) {
                setMessage(messageChannel.sendMessage(defaultMessage.build()).complete());
            } else {
                throw new InvalidMessageIDException(exception, guild, messageChannel, messageID);
            }
        }
        messageValid = true;

        resolved = true;
        return true;
    }

    public boolean areEntriesValid() {
        return guildID != 0 && guild != null && messageChannelID != 0 && messageChannel != null && messageID != 0 && message != null;
    }

    /**
     * Some exceptions will not be re-thrown. If message cannot be edited, it will try to send new one.
     * @return True if message was sent / edited
     */
    public boolean sendOrEditMessage(MessageBuilder messageBuilder) {
        if (message == null) {
            if (messageChannel != null) {
                try {
                    message = messageChannel.retrieveMessageById(messageID).complete();
                } catch (Exception ignored) {
                }

                if (message != null) {
                    message.editMessage(messageBuilder.build()).complete();
                    setMessage(message);
                } else {
                    setMessage(messageChannel.sendMessage(messageBuilder.build()).complete());
                }

                return true;
            }
        } else {
            try {
                message.editMessage(messageBuilder.build()).complete();
            } catch (Exception ignored) {
                setMessage(messageChannel.sendMessage(messageBuilder.build()).complete());
            }
            return true;
        }

        return false;
    }

    // -- Setters -- //

    public ManagedMessage setMessage(Message message) {
        if (message == null) {
            this.message = null;
            this.messageID = 0;
            this.messageValid = false;
            return this;
        }

        this.message = message;
        this.messageID = message.getIdLong();
        this.messageValid = true;

        return this;
    }

    public ManagedMessage setGuild(@NonNull Guild guild) {
        this.guild = guild;
        this.guildID = guild.getIdLong();
        this.guildValid = true;

        this.messageValid = false;
        this.messageChannelValid = false;

        return this;
    }

    public ManagedMessage setMessageChannel(@NonNull MessageChannel messageChannel) {
        this.messageChannel = messageChannel;
        this.messageChannelID = messageChannel.getIdLong();
        this.messageChannelValid = true;

        this.messageValid = false;

        return this;
    }

    // -- Java -- //

    @Override
    public String toString() {
        return "ManagedMessage{" + "name='" + name + '\'' + ", guildID=" + guildID + ", messageChannelID=" + messageChannelID + ", guild=" + guild + ", message=" + message + ", messageChannel=" + messageChannel + ", messageID=" + messageID + ", guildValid=" + guildValid + ", messageChannelValid=" + messageChannelValid + ", messageValid=" + messageValid + ", resolved=" + resolved + ", defaultMessage=" + defaultMessage + '}';
    }
}