package dev.mayuna.mayusjdautils.exceptions;

import com.google.gson.JsonObject;
import lombok.Getter;

public class InvalidJsonException extends RuntimeException {

    private final @Getter JsonObject jsonObject;

    public InvalidJsonException(String message, JsonObject jsonObject) {
        super(message);
        this.jsonObject = jsonObject;
    }

    public InvalidJsonException(String message, JsonObject jsonObject, Exception exception) {
        super(message, exception);
        this.jsonObject = jsonObject;
    }
}
