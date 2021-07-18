package lilmayu.mayusjdautilities.data;

import lilmayu.mayusjdautilities.commands.MayuCommand;
import lilmayu.mayusjdautilities.commands.types.BaseCommandType;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MayuCoreData {

    private final static @Getter List<MayuCommand> mayuCommands = new ArrayList<>();
    private final static @Getter List<BaseCommandType> commandTypes = new ArrayList<>();

    // -- Both -- //

    public static void registerMayuCommand(@NonNull MayuCommand mayuCommand) {
        if (mayuCommand.ignoreHelp)
            return;

        addMayuCommand(mayuCommand);
        addCommandType(mayuCommand.commandType);
    }

    // -- Commands -- //

    public static void addMayuCommand(@NonNull MayuCommand mayuCommand) {
        if (!doesExistMayuCommand(mayuCommand)) {
            mayuCommands.add(mayuCommand);
        }
    }

    public static boolean doesExistMayuCommand(@NonNull MayuCommand mayuCommand) {
        return doesExistMayuCommand(mayuCommand.getName());
    }

    public static boolean doesExistMayuCommand(@NonNull String nameOrAlias) {
        return getMayuCommand(nameOrAlias) != null;
    }

    public static MayuCommand getMayuCommand(@NonNull String nameOrAlias) {
        for (MayuCommand mayuCommand : mayuCommands) {
            if (mayuCommand.getName().equalsIgnoreCase(nameOrAlias)
                    || Arrays.stream(mayuCommand.getAliases()).anyMatch(i -> i.equalsIgnoreCase(nameOrAlias))) {
                return mayuCommand;
            }
        }
        return null;
    }

    // -- Types -- //

    public static void addCommandType(@NonNull BaseCommandType commandType) {
        if (!doesExistCommandType(commandType)) {
            commandTypes.add(commandType);
        }
    }

    public static boolean doesExistCommandType(@NonNull BaseCommandType commandType) {
        return doesExistCommandType(commandType.getName());
    }

    public static boolean doesExistCommandType(@NonNull String commandTypeName) {
        return getCommandType(commandTypeName) != null;
    }

    public static BaseCommandType getCommandType(@NonNull String commandTypeName) {
        for (BaseCommandType commandType : commandTypes) {
            if (commandType.getName().equalsIgnoreCase(commandTypeName)) {
                return commandType;
            }
        }
        return null;
    }

    // -- Others -- //

    public static List<MayuCommand> getMayuCommandsWithType(@NonNull BaseCommandType commandType) {
        List<MayuCommand> commands = new ArrayList<>();
        mayuCommands.forEach(mayuCommand -> {
            if (mayuCommand.isCommandType(commandType)) {
                commands.add(mayuCommand);
            }
        });
        return commands;
    }
}
