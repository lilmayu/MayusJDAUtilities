package dev.mayuna.mayusjdautils.lang;

import lombok.Getter;
import lombok.Setter;

public class LanguageSettings {

    private @Getter @Setter Messages messages = new Messages();
    private @Getter @Setter Other other = new Other();

    public static class Messages {

        private @Getter @Setter String exceptionOccurredMessage = "Exception occurred! See stack trace above.";
    }

    public static class Other {

        private @Getter @Setter String error = "Error";
        private @Getter @Setter String warning = "Warning";
        private @Getter @Setter String information = "Information";
        private @Getter @Setter String success = "Success";
    }
}
