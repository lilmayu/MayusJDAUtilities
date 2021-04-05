package lilmayu.mayusjdautilities.managed;

import com.google.gson.JsonObject;
import lilmayu.mayusjdautilities.exceptions.*;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class ManagedMessageChannel {

    // IDs
    private @Getter final long userID;
    private @Getter final long guildID;
    private @Getter final long messageChannelID;
    // User
    private @Getter final boolean isUser;
    // Name
    private @Getter @Setter String name;
    // Discord Data
    private @Getter User user;
    private @Getter Guild guild;
    private @Getter MessageChannel messageChannel;
    // Checks
    private @Getter boolean userValid = false;
    private @Getter boolean guildValid = false;
    private @Getter boolean messageChannelValid = false;
    private @Getter boolean resolved = false;

    public ManagedMessageChannel(String name, long userID) {
        this.name = name;
        this.userID = userID;
        this.isUser = true;

        this.guildID = 0;
        this.messageChannelID = 0;
    }

    public ManagedMessageChannel(String name, long guildID, long messageChannelID) {
        this.name = name;
        this.guildID = guildID;
        this.messageChannelID = messageChannelID;

        this.userID = 0;
        this.isUser = false;
    }

    public ManagedMessageChannel(String name, User user) {
        this.name = name;
        this.userID = user.getIdLong();
        this.user = user;
        this.isUser = true;

        this.guildID = 0;
        this.messageChannelID = 0;
    }

    public ManagedMessageChannel(String name, Guild guild, MessageChannel messageChannel) {
        this.name = name;
        this.guildID = guild.getIdLong();
        this.guild = guild;
        this.messageChannelID = messageChannel.getIdLong();
        this.messageChannel = messageChannel;

        this.userID = 0;
        this.isUser = false;
    }

    public static ManagedMessageChannel fromJsonObject(JsonObject jsonObject) {
        String name = null;
        long userID, guildID, messageChannelID;
        try {
            name = jsonObject.get("name").getAsString();
            boolean isUser = jsonObject.get("isUser").getAsBoolean();

            if (isUser) {
                userID = jsonObject.get("userID").getAsLong();
                return new ManagedMessageChannel(name, userID);
            } else {
                guildID = jsonObject.get("guildID").getAsLong();
                messageChannelID = jsonObject.get("messageChannelID").getAsLong();
                return new ManagedMessageChannel(name, guildID, messageChannelID);
            }
        } catch (NullPointerException nullPointerException) {
            throw new InvalidJsonException("Invalid json for ManagedMessageChannel with name: " + name, jsonObject);
        }
    }

    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", name);
        jsonObject.addProperty("isUser", isUser);

        if (isUser) {
            jsonObject.addProperty("userID", userID);
        } else {
            jsonObject.addProperty("guildID", guildID);
            jsonObject.addProperty("messageChannelID", messageChannelID);
        }

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

        if (isUser) {
            user = jda.retrieveUserById(userID).complete();
            if (user == null) {
                throw new InvalidUserIDException(userID);
            }
            userValid = true;
            try {
                messageChannel = user.openPrivateChannel().complete();
            } catch (RuntimeException exception) {
                throw new FailedToOpenPrivateChannelException(exception, user);
            }
            messageChannelValid = true;
        } else {
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
        }
        resolved = true;
        return true;
    }

    public boolean checkEntries() {
        if (isUser) {
            if (userID == 0 || user == null) {
                return false;
            }
            try {
                messageChannel = user.openPrivateChannel().complete();
            } catch (RuntimeException exception) {
                throw new FailedToOpenPrivateChannelException(exception, user);
            }
        } else {
            return guildID != 0 && guild != null && messageChannelID != 0 && messageChannel != null;
        }
        return true;
    }

    @Override
    public String toString() {
        String string = "{";

        string += "isUser=" + isUser + ";";
        string += "userID=" + userID + ";";
        string += "guildID=" + guildID + ";";
        string += "messageChannelID=" + messageChannelID + ";";
        string += "resolved=" + resolved + ";";
        string += "userValid=" + userValid + ";";
        string += "guildValid=" + guildValid + ";";
        string += "messageChannelValid=" + messageChannelValid + ";";
        string += "user=" + user + ";";
        string += "guild=" + guild.toString() + ";";
        string += "messageChannel=" + messageChannel.toString() + ";";

        return string + "}";
    }
}
