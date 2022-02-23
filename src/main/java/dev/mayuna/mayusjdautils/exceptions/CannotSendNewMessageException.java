package dev.mayuna.mayusjdautils.exceptions;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

public class CannotSendNewMessageException extends RuntimeException {

    private final @Getter Throwable throwable;
    private final @Getter Guild guild;
    private final @Getter TextChannel textChannel;

    public CannotSendNewMessageException(Throwable throwable, Guild guild, TextChannel textChannel) {
        super("Cannot send new message into " + textChannel + " in guild " + guild + "!", throwable);
        this.throwable = throwable;
        this.guild = guild;
        this.textChannel = textChannel;
    }
}
