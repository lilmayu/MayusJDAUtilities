package dev.mayuna.mayusjdautils.managed;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.mayuna.mayusjdautils.exceptions.FailedToGetTextChannelGuildException;
import dev.mayuna.mayusjdautils.exceptions.InvalidGuildIDException;
import dev.mayuna.mayusjdautils.exceptions.InvalidJsonException;
import dev.mayuna.mayusjdautils.exceptions.InvalidMessageIDException;
import dev.mayuna.mayusjdautils.utils.DiscordUtils;
import dev.mayuna.mayusjsonutils.data.Savable;
import dev.mayuna.mayusjsonutils.objects.MayuJson;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class ManagedMessage implements Savable {

    // Name
    private @Getter String name;

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
    private @Getter @Setter MessageBuilder defaultMessage = new MessageBuilder().setEmbeds(DiscordUtils.getDefaultEmbed().setTimestamp(null).build());

    // -- Constructs -- //

    public ManagedMessage(String name) {
        this.name = name;
    }

    public ManagedMessage(JsonObject jsonObject) {
        fromJsonObject(jsonObject);
    }

    public ManagedMessage(String name, long guildID, long messageChannelID) {
        this.name = name;
        this.guildID = guildID;
        this.messageChannelID = messageChannelID;
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

    public void fromJsonObject(JsonObject jsonObject) {
        try {
            MayuJson mayuJson = new MayuJson(jsonObject);
            this.name = mayuJson.getOrNull("name").getAsString();
            this.guildID = mayuJson.getOrCreate("guildID", new JsonPrimitive(0)).getAsLong();
            this.messageChannelID = mayuJson.getOrCreate("messageChannelID", new JsonPrimitive(0)).getAsLong();
            this.messageID = mayuJson.getOrCreate("messageID", new JsonPrimitive(0)).getAsLong();
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
     *
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
