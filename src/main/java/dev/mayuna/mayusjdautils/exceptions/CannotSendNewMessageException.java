package dev.mayuna.mayusjdautils.exceptions;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

public class CannotSendNewMessageException extends RuntimeException {

    private final @Getter Throwable throwable;
    private final @Getter Guild guild;
    private final @Getter GuildMessageChannel guildMessageChannel;

    public CannotSendNewMessageException(Throwable throwable, Guild guild, GuildMessageChannel guildMessageChannel) {
        super("Cannot send new message into " + guildMessageChannel + " in guild " + guild + "!", throwable);
        this.throwable = throwable;
        this.guild = guild;
        this.guildMessageChannel = guildMessageChannel;
    }
}
