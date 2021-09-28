package dev.mayuna.mayusjdautils.exceptions;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class InvalidMessageIDException extends RuntimeException {

    private final @Getter ErrorResponseException errorResponseException;
    private final @Getter Guild guild;
    private final @Getter MessageChannel messageChannel;
    private final @Getter long messageID;

    public InvalidMessageIDException(ErrorResponseException errorResponseException, Guild guild, MessageChannel messageChannel, long messageID) {
        super("Invalid Message ID: " + messageID + " in Guild: " + guild.getIdLong() + " in TextChannel: " + messageChannel.getIdLong() + " - You will have to send new Message to this TextChannel and save it into ManagedMessage. ErrorResponseException is in this exception.");
        this.errorResponseException = errorResponseException;
        this.guild = guild;
        this.messageChannel = messageChannel;
        this.messageID = messageID;
    }
}
