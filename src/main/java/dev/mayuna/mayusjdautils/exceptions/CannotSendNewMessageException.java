package dev.mayuna.mayusjdautils.exceptions;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class CannotSendNewMessageException extends RuntimeException {

    private final @Getter Exception exception;
    private final @Getter Guild guild;
    private final @Getter TextChannel textChannel;

    public CannotSendNewMessageException(Exception exception, Guild guild, TextChannel textChannel) {
        super("Cannot send new message into " + textChannel + " in guild " + guild + "!", exception);
        this.exception = exception;
        this.guild = guild;
        this.textChannel = textChannel;
    }
}
