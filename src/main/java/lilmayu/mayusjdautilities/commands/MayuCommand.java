package lilmayu.mayusjdautilities.commands;

import com.jagrosh.jdautilities.command.Command;
import lilmayu.mayusjdautilities.commands.types.BaseCommandType;
import lilmayu.mayusjdautilities.commands.types.GeneralCommandType;
import lilmayu.mayusjdautilities.data.MayuCoreData;

public abstract class MayuCommand extends Command {

    public String description = null;
    public String syntax = null;
    public String[] examples = null;
    public BaseCommandType commandType = new GeneralCommandType();
    public boolean ignoreHelp = false;

    public MayuCommand() {
        MayuCoreData.addMayuCommand(this);
        MayuCoreData.addCommandType(this.commandType);
    }

    public boolean isCommandType(BaseCommandType commandType) {
        return this.commandType.equals(commandType);
    }

    @Override
    public String toString() {
        return name;
    }
}

