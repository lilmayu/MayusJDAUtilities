package lilmayu.mayusjdautilities.helpers;

import lilmayu.mayusjdautilities.objects.Argument;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ArgumentHelper {

    private @Getter String allArguments;

    private @Getter List<Argument> argumentList;

    public ArgumentHelper(String arguments) {
        processArguments(arguments, " ");
    }

    public ArgumentHelper(String arguments, @NotNull String separator) {
        processArguments(arguments, separator);
    }

    public boolean hasAnyArguments() {
        if (allArguments == null)
            return false;
        return hasArgumentByIndex(0);
    }

    public boolean hasArgumentByIndex(int index) {
        if (index >= 0 && index < argumentList.size()) {
            String argument = argumentList.get(index).getValue();
            return !argument.equals("");
        }
        return false;
    }

    public Argument getArgumentByIndex(int index) {
        if (hasArgumentByIndex(index)) {
            return argumentList.get(index);
        }
        return null;
    }

    private void processArguments(String arguments, String separator) {
        if (arguments == null) {
            arguments = "";
        }

        this.allArguments = arguments;

        argumentList = new ArrayList<>();

        String[] argumentsArray = arguments.split(separator);

        for (String stringArgument : argumentsArray) {
            Argument argument = new Argument(stringArgument);
            argumentList.add(argument);
        }
    }
}
