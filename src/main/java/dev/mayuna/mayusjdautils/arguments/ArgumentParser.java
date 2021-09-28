package dev.mayuna.mayusjdautils.arguments;

import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArgumentParser {

    // Data
    private @Getter String arguments;
    private @Getter ArgumentSeparator argumentSeparator;

    // Parsed
    private @Getter List<Argument> argumentList = new ArrayList<>();

    /**
     * Creates {@link ArgumentParser} object with specified string
     * <br>
     * Default {@link ArgumentSeparator} is SPACE
     *
     * @param arguments Set of arguments (usually from user's message)
     */
    public ArgumentParser(@NonNull String arguments) {
        this.arguments = arguments;
        this.argumentSeparator = ArgumentSeparator.SPACE;

        if (!arguments.equals("")) {
            parseArguments();
        }
    }

    /**
     * Creates {@link ArgumentParser} object with specified string
     *
     * @param arguments         Set of arguments (usually from user's message)
     * @param argumentSeparator {@link ArgumentSeparator} specifier - This will change the way that ArgumentParser parses arguments
     */
    public ArgumentParser(@NonNull String arguments, @NonNull ArgumentSeparator argumentSeparator) {
        this.arguments = arguments;
        this.argumentSeparator = argumentSeparator;

        if (!arguments.equals("")) {
            parseArguments();
        }
    }

    /**
     * Parses supplied arguments. You do not have to call it after construction of {@link ArgumentParser} since it is called automatically
     *
     * @return Returns itself
     */
    public ArgumentParser parseArguments() {
        argumentList = new ArrayList<>();

        if (argumentSeparator == ArgumentSeparator.LINE) {
            parseByLine();
        } else {
            parseBySpace();
        }

        return this;
    }

    /**
     * Checks if there were any parsed arguments
     *
     * @return true if there were any parsed arguments
     */
    public boolean hasAnyArguments() {
        return !argumentList.isEmpty();
    }

    /**
     * Checks if there was any parsed argument on specified index
     *
     * @param index Specified index for arguments position
     *
     * @return true if there was any parsed argument
     */
    public boolean hasArgumentAtIndex(int index) {
        return getArgumentAtIndex(index) != null;
    }

    /**
     * Gets {@link Argument} at specified index
     *
     * @param index Specified index for arguments position
     *
     * @return Possibly-null {@link Argument} if exists
     */
    public Argument getArgumentAtIndex(int index) {
        try {
            return argumentList.get(index);
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Gets with and all arguments after specified index <br>
     * Example: <br>
     * - Argument list "foo bar baz" <br>
     *  - With index 0 -> "foo bar baz" <br>
     *  - With index 1 -> "bar baz" <br>
     *
     * @param index With and after which index it should return
     *
     * @return {@link Argument}, can have multiple arguments in itself
     */
    public Argument getAllArgumentsAfterIndex(int index) {
        StringBuilder stringBuilder = new StringBuilder();

        if (index < 0) {
            return new Argument(arguments);
        }

        for (int x = index; x < argumentList.size(); x++) {
            stringBuilder.append(argumentList.get(x).getValue()).append(" ");
        }

        if (stringBuilder.length() == 0) {
            return null;
        }

        return new Argument(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
    }

    /**
     * Gets arguments between two indexes
     *
     * @param startIndex Index from which it should start
     * @param endIndex index to which it should stop
     * @return {@link Argument}, can have multiple arguments in itself
     */
    public Argument getAllArgumentsBetweenIndexes(int startIndex, int endIndex) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int x = startIndex; x < endIndex + 1 && x < argumentList.size(); x++) {
            stringBuilder.append(argumentList.get(x).getValue()).append(" ");
        }

        return new Argument(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
    }

    /**
     * Sets {@link ArgumentSeparator} flag. You do not have to call {@link #parseArguments()} since this method calls it automatically.
     *
     * @param argumentSeparator {@link ArgumentSeparator} flag
     * @return Return itself, re-parsed
     */
    public ArgumentParser setArgumentSeparator(ArgumentSeparator argumentSeparator) {
        this.argumentSeparator = argumentSeparator;
        return parseArguments();
    }

    // Private methods

    private void parseBySpace() {
        Arrays.asList(arguments.split(" ")).forEach(parsedArgument -> argumentList.add(new Argument(parsedArgument)));
    }

    private void parseByLine() {
        Arrays.asList(arguments.split("\\|")).forEach(parsedArgument -> {
            argumentList.add(new Argument(removeSpaces(parsedArgument)));
        });
    }

    private String removeSpaces(String string) {
        StringBuilder stringBuilder = new StringBuilder(string);

        for (int x = 0; x < stringBuilder.length() - 1; x++) {
            if (stringBuilder.charAt(0) == ' ') {
                stringBuilder.deleteCharAt(0);
            } else {
                break;
            }
        }

        for (int x = stringBuilder.length() - 1; x > 0; x--) {
            if (stringBuilder.charAt(x) == ' ') {
                stringBuilder.deleteCharAt(x);
            } else {
                break;
            }
        }

        return stringBuilder.toString();
    }
}
