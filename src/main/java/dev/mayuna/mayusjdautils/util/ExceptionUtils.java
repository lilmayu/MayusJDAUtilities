package dev.mayuna.mayusjdautils.util;

import dev.mayuna.mayuslibrary.util.objects.ParsedStackTraceElement;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {

    /**
     * Formats a throwable's stack trace into a string.
     * @param throwable The throwable to format.
     * @return The formatted stack trace.
     */
    public static String formatExceptionStackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);

        StringBuilder text = new StringBuilder();

        for (String line : stringWriter.toString().split("\n")) {
            if (text.length() + line.length() > 2048) {
                break;
            }

            text.append(line).append("\n");
        }

        return text.toString();
    }

    /**
     * Formats a throwable's stack trace into a Discord compatible content field.
     * @param throwable The throwable to format.
     * @return The formatted stack trace in Discord compatible content field.
     */
    public static String formatExceptionInformationField(Throwable throwable) {
        ParsedStackTraceElement parsedStackTraceElement = new ParsedStackTraceElement(throwable.getStackTrace()[0]);

        String string = "```md";

        string += "Exception: " + throwable + "\n";
        string += " - Class.: " + parsedStackTraceElement.getClassName() + "\n";
        string += " - Method: #" + parsedStackTraceElement.getMethodName() + "()\n";
        string += " - File..: " + parsedStackTraceElement.getFileName() + "\n";
        string += " - Line..: " + parsedStackTraceElement.getLineNumber() + "\n";

        return string + "```";
    }
}
