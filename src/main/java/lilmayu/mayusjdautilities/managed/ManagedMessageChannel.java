package lilmayu.mayusjdautilities.managed;

import com.google.gson.JsonObject;
import lilmayu.mayusjdautilities.exceptions.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class ManagedMessageChannel {

    // IDs
    private @Getter long userID;
    private @Getter long guildID;
    private @Getter long messageChannelID;

    // User
    private @Getter boolean isUser;

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

    // -- Constructs -- //

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

    public ManagedMessageChannel(String name, @NonNull User user) {
        this.name = name;

        setUser(user);

        this.guildID = 0;
        this.messageChannelID = 0;
    }

    public ManagedMessageChannel(String name, @NonNull Guild guild, @NonNull MessageChannel messageChannel) {
        this.name = name;

        setGuild(guild);

        setMessageChannel(messageChannel);

        this.userID = 0;
        this.isUser = false;
    }

    // -- JSON stuff -- //

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

    // -- Main Logic -- //

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
        }
        messageChannelValid = true;
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

    // -- Setters -- //

    public void setUser(User user) {
        this.isUser = true;
        this.user = user;
        this.userID = user.getIdLong();
        this.userValid = true;

        this.guildValid = false;
    }

    public void setGuild(Guild guild) {
        this.isUser = false;
        this.guild = guild;
        this.guildID = guild.getIdLong();
        this.guildValid = false;

        this.messageChannelValid = false;
    }

    public void setMessageChannel(MessageChannel messageChannel) {
        this.messageChannel = messageChannel;
        this.messageChannelID = messageChannel.getIdLong();
        this.messageChannelValid = false;
    }

    // -- Java -- //

    @Override
    public String toString() {
        return "ManagedMessageChannel{" +
                "userID=" + userID +
                ", guildID=" + guildID +
                ", messageChannelID=" + messageChannelID +
                ", isUser=" + isUser +
                ", name='" + name + '\'' +
                ", user=" + user +
                ", guild=" + guild +
                ", messageChannel=" + messageChannel +
                ", userValid=" + userValid +
                ", guildValid=" + guildValid +
                ", messageChannelValid=" + messageChannelValid +
                ", resolved=" + resolved +
                '}';
    }
}
