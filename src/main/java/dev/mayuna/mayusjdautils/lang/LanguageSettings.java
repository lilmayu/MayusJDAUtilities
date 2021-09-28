package dev.mayuna.mayusjdautils.lang;

import lombok.Getter;
import lombok.Setter;

public class LanguageSettings {

    // -- Messages -- //

    public static class Messages {

        private static @Getter @Setter String invalidSyntax = "Invalid syntax! Please, see `help` command.";
        private static @Getter @Setter String invalidSyntaxHint = "Invalid syntax! Syntax for this command is `{syntax}`.";
        private static @Getter @Setter String unknownCommand = "Unknown command! Please, use `help` command.";

        private static @Getter @Setter String exceptionOccurredMessage = "Exception occurred! See stack trace above.";
    }

    // -- HelpCommand -- //

    public static class HelpCommand {

        private static @Getter @Setter String embedHelpCommandHomeDescription = "Please, use `help <command>` for command-specific help.";
        private static @Getter @Setter String commandDescription = "This is default help command in Mayu's JDA Utilities. It lists every command, which was registered by developer.";
        private static @Getter @Setter String title = "Help Command";
        private static @Getter @Setter String description = "For more per-command information, please use `help <command>`";
    }

    // -- Other -- //

    public static class Other {
        private static @Getter @Setter String information = "Information";
        private static @Getter @Setter String close = "Close";
    }
}
