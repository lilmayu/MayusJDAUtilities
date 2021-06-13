package lilmayu.mayusjdautilities.commands;

import com.jagrosh.jdautilities.command.Command;
import lilmayu.mayusjdautilities.objects.CommandType;

public abstract class MayuCommand extends Command {

    public String description = "Null!";
    public String syntax = "Null!";
    public String[] examples = new String[]{"Null!"};
    public CommandType commandType = CommandType.GENERAL;

    @Override
    public String toString() {
        return name;
    }
}

