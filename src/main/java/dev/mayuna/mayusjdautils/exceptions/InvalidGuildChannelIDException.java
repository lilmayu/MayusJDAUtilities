package dev.mayuna.mayusjdautils.exceptions;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

public class InvalidGuildChannelIDException extends RuntimeException {

    private final @Getter Guild guild;
    private final @Getter long guildChannelId;

    public InvalidGuildChannelIDException(Guild guild, long guildChannelId) {
        super("Failed to get Guild Channel from guild " + guild.getIdLong() + " with Guild channel ID " + guildChannelId + " - Probably does not exist?");
        this.guild = guild;
        this.guildChannelId = guildChannelId;
    }
}
