package lilmayu.mayusjdautilities.commands;

import com.jagrosh.jdautilities.command.Command;
import lilmayu.mayusjdautilities.objects.CommandType;

public abstract class MayuCommand extends Command {

    public String description = "N/A";
    public String syntax = "N/A";
    public String[] examples = new String[]{"N/A"};
    public CommandType commandType = CommandType.GENERAL;

    @Override
    public String toString() {
        return name;
    }
}

