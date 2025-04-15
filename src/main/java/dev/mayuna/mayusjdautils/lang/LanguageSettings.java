package dev.mayuna.mayusjdautils.lang;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LanguageSettings {

    private Messages messages = new Messages();
    private Other other = new Other();

    @Getter
    @Setter
    public static class Messages {

        private String exceptionOccurredMessage = "Exception occurred! See stack trace above.";
    }

    @Getter
    @Setter
    public static class Other {

        private String error = "Error";
        private String warning = "Warning";
        private String information = "Information";
        private String success = "Success";
    }
}
