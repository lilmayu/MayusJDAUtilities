package lilmayu.mayusjdautilities.exceptions;

import lombok.Getter;

public class InvalidGuildIDException extends RuntimeException {

    private final @Getter long guildID;

    public InvalidGuildIDException(long guildID) {
        super("Invalid Guild ID: " + guildID);
        this.guildID = guildID;
    }
}
