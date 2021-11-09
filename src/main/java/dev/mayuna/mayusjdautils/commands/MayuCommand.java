package dev.mayuna.mayusjdautils.commands;

import com.jagrosh.jdautilities.command.SlashCommand;
import dev.mayuna.mayusjdautils.commands.types.BaseCommandType;
import dev.mayuna.mayusjdautils.commands.types.GeneralCommandType;

public abstract class MayuCommand extends SlashCommand {

    public String description = "No description.";
    public String syntax = "No syntax.";
    public String[] examples = new String[]{"No examples."};
    public BaseCommandType commandType = new GeneralCommandType();
    public boolean indexInHelpCommand = true;

    @Override
    public String toString() {
        return name;
    }
}
