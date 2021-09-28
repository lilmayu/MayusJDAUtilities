package dev.mayuna.mayusjdautils.data;

import dev.mayuna.mayusjdautils.commands.MayuCommand;
import dev.mayuna.mayusjdautils.commands.types.BaseCommandType;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MayuCoreData {

    private final static @Getter List<MayuCommand> MAYU_COMMANDS = new ArrayList<>();
    private final static @Getter List<BaseCommandType> COMMAND_TYPES = new ArrayList<>();

    // -- Both -- //

    /**
     * Registers {@link MayuCommand} into internal registers. Does not register commands with flag "indexInHelpCommand" set to true.
     *
     * @param mayuCommand Non-null {@link MayuCommand} object
     */
    public static void registerCommand(@NonNull MayuCommand mayuCommand) {
        if (!mayuCommand.indexInHelpCommand)
            return;

        addCommand(mayuCommand);
        addCommandType(mayuCommand.commandType);
    }

    // -- Commands -- //

    public static void addCommand(@NonNull MayuCommand mayuCommand) {
        if (!doesExistCommand(mayuCommand)) {
            MAYU_COMMANDS.add(mayuCommand);
        }
    }

    /**
     * Checks if command exists in internal registers
     *
     * @param mayuCommand Non-null {@link MayuCommand} object
     *
     * @return true if exists
     */
    public static boolean doesExistCommand(@NonNull MayuCommand mayuCommand) {
        return doesExistCommand(mayuCommand.getName());
    }

    /**
     * Checks if command exists in internal registers
     *
     * @param nameOrAlias Non-null name or alias of a command
     *
     * @return true if exists
     */
    public static boolean doesExistCommand(@NonNull String nameOrAlias) {
        return getCommand(nameOrAlias) != null;
    }

    /**
     * Gets {@link MayuCommand} if exists
     *
     * @param nameOrAlias Non-null name or alias of a command
     *
     * @return Possibly-null {@link MayuCommand} if exists
     */
    public static MayuCommand getCommand(@NonNull String nameOrAlias) {
        for (MayuCommand mayuCommand : MAYU_COMMANDS) {
            if (mayuCommand.getName().equalsIgnoreCase(nameOrAlias) || Arrays.stream(mayuCommand.getAliases()).anyMatch(i -> i.equalsIgnoreCase(nameOrAlias))) {
                return mayuCommand;
            }
        }
        return null;
    }

    // -- Types -- //

    public static void addCommandType(@NonNull BaseCommandType commandType) {
        if (!doesExistCommandType(commandType)) {
            COMMAND_TYPES.add(commandType);
        }
    }

    /**
     * Checks if command type exists in internal registers
     *
     * @param commandType Non-null {@link BaseCommandType} object
     *
     * @return true if exists
     */
    public static boolean doesExistCommandType(@NonNull BaseCommandType commandType) {
        return doesExistCommandType(commandType.getName());
    }

    /**
     * Checks if command type exists in internal registers
     *
     * @param commandTypeName Non-null name of command type
     *
     * @return true if exists
     */
    public static boolean doesExistCommandType(@NonNull String commandTypeName) {
        return getCommandType(commandTypeName) != null;
    }

    /**
     * Gets {@link BaseCommandType} if exists
     *
     * @param commandTypeName Non-null name of command type
     *
     * @return Possibly-null {@link BaseCommandType} if exists
     */
    public static BaseCommandType getCommandType(@NonNull String commandTypeName) {
        for (BaseCommandType commandType : COMMAND_TYPES) {
            if (commandType.getName().equalsIgnoreCase(commandTypeName)) {
                return commandType;
            }
        }
        return null;
    }

    // -- Others -- //

    /**
     * Gets all commands with specified command type
     *
     * @param commandType Non-null {@link BaseCommandType} object
     *
     * @return List of all commands with same command type
     */
    public static List<MayuCommand> getCommandsWithType(@NonNull BaseCommandType commandType) {
        List<MayuCommand> commands = new ArrayList<>();
        MAYU_COMMANDS.forEach(mayuCommand -> {
            if (mayuCommand.commandType.equals(commandType)) {
                commands.add(mayuCommand);
            }
        });
        return commands;
    }
}
