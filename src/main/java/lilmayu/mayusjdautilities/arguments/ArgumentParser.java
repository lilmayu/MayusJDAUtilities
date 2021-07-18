package lilmayu.mayusjdautilities.arguments;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArgumentParser {

    // Data
    private @Getter String arguments;
    private @Getter ArgumentSeparator argumentSeparator;

    // Parsed
    private @Getter List<Argument> argumentList = new ArrayList<>();


    public ArgumentParser(String arguments) {
        this.arguments = arguments;
        this.argumentSeparator = ArgumentSeparator.SPACE;

        if (!arguments.equals("")) {
            parseArguments();
        }
    }

    public ArgumentParser(String arguments, ArgumentSeparator argumentSeparator) {
        this.arguments = arguments;
        this.argumentSeparator = argumentSeparator;

        if (!arguments.equals("")) {
            parseArguments();
        }
    }

    public ArgumentParser parseArguments() {
        argumentList = new ArrayList<>();

        if (argumentSeparator == ArgumentSeparator.LINE) {
            parseByLine();
        } else {
            parseBySpace();
        }

        return this;
    }

    public boolean hasAnyArguments() {
        return !argumentList.isEmpty();
    }

    public boolean hasArgumentAtIndex(int index) {
        return getArgumentAtIndex(index) != null;
    }

    public Argument getArgumentAtIndex(int index) {
        try {
            return argumentList.get(index);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public Argument getAllArgumentsAfterIndex(int index) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int x = index; x < argumentList.size(); x++) {
            stringBuilder.append(argumentList.get(x).getValue()).append(" ");
        }

        if (stringBuilder.length() == 0) {
            return null;
        }

        return new Argument(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
    }

    public Argument getAllArgumentsBetweenIndexes(int startIndex, int endIndex) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int x = startIndex; x < endIndex + 1 && x < argumentList.size(); x++) {
            stringBuilder.append(argumentList.get(x).getValue()).append(" ");
        }

        return new Argument(stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString());
    }

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
