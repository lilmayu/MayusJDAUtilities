package dev.mayuna.mayusjdautils.exceptions;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

public class FailedToGetTextChannelGuildException extends RuntimeException {

    private final @Getter Guild guild;
    private final @Getter long messageChannelID;

    public FailedToGetTextChannelGuildException(Guild guild, long messageChannelID) {
        super("Failed to get Text Channel from guild: " + guild.getIdLong() + " - Probably does not exist?");
        this.guild = guild;
        this.messageChannelID = messageChannelID;
    }
}
