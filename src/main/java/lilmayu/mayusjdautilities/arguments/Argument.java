package lilmayu.mayusjdautilities.arguments;

import lilmayu.mayusjdautilities.utils.DiscordUtils;
import lilmayu.mayuslibrary.utils.NumberUtils;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;

public class Argument {

    // Data
    private final @Getter String value;

    public Argument(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Gets argument's value as {@link Number}
     *
     * @return {@link Number} if parsable, otherwise null
     */
    public Number getValueAsNumber() {
        return NumberUtils.parseNumber(value);
    }

    public User getValueAsUser(JDA jda) {
        return DiscordUtils.isUserMention(value) ? jda.retrieveUserById(DiscordUtils.getMentionID(value)).complete() : null;
    }

    public Role getValueAsRole(Guild guild) {
        return DiscordUtils.isRoleMention(value) ? guild.getRoleById(DiscordUtils.getMentionID(value)) : null;
    }

    public MessageChannel getValueAsChannel(Guild guild) {
        return DiscordUtils.isChannelMention(value) ? guild.getTextChannelById(DiscordUtils.getMentionID(value)) : null;
    }

    public Emote getValueAsEmote(Guild guild) {
        return DiscordUtils.isEmoteMention(value) ? guild.retrieveEmoteById(DiscordUtils.getMentionID(value)).complete() : null;
    }
}
