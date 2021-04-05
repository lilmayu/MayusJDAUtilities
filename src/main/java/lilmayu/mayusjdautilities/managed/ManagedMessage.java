package lilmayu.mayusjdautilities.managed;

import com.google.gson.JsonObject;
import lilmayu.mayusjdautilities.exceptions.FailedToGetTextChannelGuildException;
import lilmayu.mayusjdautilities.exceptions.InvalidGuildIDException;
import lilmayu.mayusjdautilities.exceptions.InvalidJsonException;
import lilmayu.mayusjdautilities.exceptions.InvalidMessageIDException;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class ManagedMessage {

    // Name
    private @Getter final String name;
    // IDs
    private final @Getter long guildID;
    private final @Getter long messageChannelID;
    // Discord Data
    private @Getter Guild guild;
    private @Getter Message message;
    private @Getter MessageChannel messageChannel;
    private @Getter long messageID;
    // Checks
    private @Getter boolean guildValid = false;
    private @Getter boolean messageChannelValid = false;
    private @Getter boolean messageValid = false;
    private @Getter boolean resolved = false;

    public ManagedMessage(String name, long guildID, long messageChannelID, long messageID) {
        this.name = name;
        this.guildID = guildID;
        this.messageChannelID = messageChannelID;
        this.messageID = messageID;
    }

    public ManagedMessage(String name, Guild guild, MessageChannel messageChannel, Message message) {
        this.name = name;
        this.guildID = guild.getIdLong();
        this.guild = guild;
        this.messageChannelID = messageChannel.getIdLong();
        this.messageChannel = messageChannel;
        this.messageID = message.getIdLong();
        this.message = message;
    }

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

    public boolean updateEntries(JDA jda) {
        return updateEntries(jda, false);
    }

    public boolean updateEntries(JDA jda, boolean force) {
        if (checkEntries()) {
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

        message = messageChannel.retrieveMessageById(messageID).complete();
        if (message == null) {
            throw new InvalidMessageIDException(guild, messageChannel, messageID);
        }
        messageValid = true;
        resolved = true;
        return true;
    }

    public boolean checkEntries() {
        return guildID != 0 && guild != null && messageChannelID != 0 && messageChannel != null && messageID != 0 && message != null;
    }

    public void setMessage(Message message) {
        this.message = message;
        this.messageID = message.getIdLong();
    }

    @Override
    public String toString() {
        String string = "{";

        string += "guildID=" + guildID + ";";
        string += "messageChannelID=" + messageChannelID + ";";
        string += "messageID=" + messageID + ";";
        string += "resolved=" + resolved + ";";
        string += "guildValid=" + guildValid + ";";
        string += "messageChannelValid=" + messageChannelValid + ";";
        string += "messageValid=" + messageValid + ";";
        string += "guild=" + guild.toString() + ";";
        string += "messageChannel=" + messageChannel.toString() + ";";
        string += "message=" + message.toString() + ";";

        return string + "}";
    }
}
