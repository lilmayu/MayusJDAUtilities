package dev.mayuna.mayusjdautils.exceptions;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;

public class InvalidTextChannelIDException extends RuntimeException {

    private final @Getter Guild guild;
    private final @Getter long textChannelID;

    public InvalidTextChannelIDException(Guild guild, long textChannelID) {
        super("Failed to get Text Channel from guild " + guild.getIdLong() + " with Text channel ID " + textChannelID + " - Probably does not exist?");
        this.guild = guild;
        this.textChannelID = textChannelID;
    }
}
