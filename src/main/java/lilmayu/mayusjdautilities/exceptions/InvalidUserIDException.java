package lilmayu.mayusjdautilities.exceptions;

import lombok.Getter;

public class InvalidUserIDException extends RuntimeException {

    private final @Getter long userID;

    public InvalidUserIDException(long userID) {
        super("Invalid User ID: " + userID);
        this.userID = userID;
    }
}
