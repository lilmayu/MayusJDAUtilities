package lilmayu.mayusjdautilities.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import lilmayu.mayusjdautilities.helpers.ArgumentHelper;
import lilmayu.mayusjdautilities.objects.CommandType;
import lilmayu.mayusjdautilities.utils.GeneralUtils;
import lilmayu.mayusjdautilities.utils.SystemEmotes;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;
import java.util.List;

public class MayuHelpCommand extends MayuCommand {

    private static @Getter List<MayuCommand> commands = new ArrayList<>();

    private String titleText = "Help";
    private String descriptionText = "For more information, please use `help [command]`";

    public MayuHelpCommand() {
        this.name = "help";
        this.aliases = new String[]{"help", "pomoc"};
        this.guildOnly = false;

        this.description = "Mayu's JDA Utilities Help Command";
        this.syntax = "help [command]";
        this.examples = new String[]{
                "help - Will send all visible commands",
                "help ping - Will send information about command"
        };

        commands.add(this);
    }

    public MayuCommand setTitle(String title) {
        this.titleText = title;
        return this;
    }

    public MayuCommand setDescription(String descriptionText) {
        this.descriptionText = descriptionText;
        return this;
    }

    @Override
    protected void execute(CommandEvent event) {
        ArgumentHelper argumentHelper = new ArgumentHelper(event.getArgs());

        if (argumentHelper.hasAnyArguments()) {
            MayuCommand mayuCommand = getMayuCommandByName(argumentHelper.getArgumentByIndex(0).getValue());
            event.reply(makeCommandHelp(mayuCommand).build());
        } else {
            event.reply(makeGeneralHelp().build());
        }
    }

    private EmbedBuilder makeGeneralHelp() {
        EmbedBuilder embedBuilder = GeneralUtils.makeDefaultEmbed();

        embedBuilder.setTitle(titleText);
        embedBuilder.setDescription(descriptionText);

        for (CommandType commandType : CommandType.values()) {
            MayuCommand[] mayuCommands = getCommandsByType(commandType);
            if (mayuCommands.length != 0) {
                embedBuilder.addField(commandType.toString(), GeneralUtils.makePrettyList(mayuCommands), false);
            }
        }

        return embedBuilder;
    }

    private EmbedBuilder makeCommandHelp(MayuCommand command) {
        EmbedBuilder embedBuilder = GeneralUtils.makeDefaultEmbed();

        if (command == null) {
            embedBuilder.setDescription(SystemEmotes.ERROR + " | Sorry, this command is not registered, is hidden, or does not exist!");
        } else {
            if (command.isHidden()) {
                embedBuilder.setDescription(SystemEmotes.ERROR + " | Sorry, this command is not registered, is hidden, or does not exist!");
            } else {
                embedBuilder.setTitle("Command - " + command.getName());
                embedBuilder.setDescription(command.description);

                embedBuilder.addField("Command type", command.commandType.toString(), false);
                embedBuilder.addField("Syntax", "`" + command.syntax + "`", false);
                embedBuilder.addField("Examples", makeList(command.examples, true), false);
                embedBuilder.addField("Aliases", makeList(command.getAliases(), true), false);

                embedBuilder.addField("Advanced", makeList(new String[]{
                        (command.getRequiredRole() == null ? "None" : command.getRequiredRole()),
                        "Guild only: " + command.isGuildOnly(),
                        "Cooldown: " + command.getCooldown(),
                        "Sub-commands: " + makeCommandChildrenList(command)
                }, true), false);
            }
        }

        return embedBuilder;
    }

    private MayuCommand[] getCommandsByType(CommandType commandType) {
        List<MayuCommand> mayuCommandList = new ArrayList<>();

        for (MayuCommand command : commands) {
            if (command.commandType == commandType) {
                mayuCommandList.add(command);
            }
        }

        return mayuCommandList.toArray(new MayuCommand[]{});
    }


    private String makeList(String[] array, boolean code) {
        String list = "";

        if (array == null || array.length == 0) {
            return "N/A";
        }

        for (String string : array) {
            list += string + "\n";
        }

        if (code) {
            list = "```" + list + "```";
        }
        return list;
    }

    private String makeCommandChildrenList(Command command) {
        String list = "\n";

        if (command.getChildren().length == 0) {
            list = "N/A";
        } else {
            for (Command subCommand : command.getChildren()) {
                list += " - " + subCommand.getName() + "\n";
            }
        }

        return list;
    }

    private MayuCommand getMayuCommandByName(String commandName) {
        for (MayuCommand mayuCommand : commands) {
            if (mayuCommand.getName().equalsIgnoreCase(commandName))
                return mayuCommand;
        }
        return null;
    }

    public static void registerCommand(MayuCommand mayuCommand) {
        if (mayuCommand != null)
            commands.add(mayuCommand);
    }
}
