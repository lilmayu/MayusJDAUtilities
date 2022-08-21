package dev.mayuna.mayusjdautils.lang;

import lombok.Getter;
import lombok.Setter;

public class LanguageSettings {

    public static class Messages {
        private static @Getter @Setter String exceptionOccurredMessage = "Exception occurred! See stack trace above.";
    }

    public static class Other {

        private static @Getter @Setter String information = "Information";
    }
}
