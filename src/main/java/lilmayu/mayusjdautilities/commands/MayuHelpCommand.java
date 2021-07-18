package lilmayu.mayusjdautilities.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import lilmayu.mayusjdautilities.arguments.ArgumentParser;
import lilmayu.mayusjdautilities.commands.types.BaseCommandType;
import lilmayu.mayusjdautilities.commands.types.GeneralCommandType;
import lilmayu.mayusjdautilities.data.MayuCoreData;
import lilmayu.mayusjdautilities.settings.LanguageSettings;
import lilmayu.mayusjdautilities.utils.ColorUtils;
import lilmayu.mayusjdautilities.utils.DiscordUtils;
import lilmayu.mayusjdautilities.utils.MessageUtils;
import lilmayu.mayuslibrary.utils.ArrayUtils;
import lilmayu.mayuslibrary.utils.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;

public class MayuHelpCommand extends MayuCommand {

    // -- Constructs -- //

    public MayuHelpCommand() {
        this.name = "help";
        this.description = LanguageSettings.HelpCommand.getCommandDescription();
        this.syntax = "help [command]";
        this.examples = new String[]{"help", "help ping"};
        this.commandType = new GeneralCommandType();
    }

    @Override
    protected void execute(CommandEvent event) {
        ArgumentParser argumentParser = new ArgumentParser(event.getArgs());

        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();
        embedBuilder.setTitle(LanguageSettings.HelpCommand.getTitle());
        embedBuilder.setDescription(LanguageSettings.HelpCommand.getEmbedHelpCommandHomeDescription());
        embedBuilder.setColor(ColorUtils.getDefaultColor());

        boolean canSend = true;

        if (argumentParser.hasAnyArguments()) {
            String commandName = argumentParser.getArgumentAtIndex(0).getValue();
            canSend = makeCommandSpecificHelp(embedBuilder, commandName);
        } else {
            for (BaseCommandType commandType : MayuCoreData.getCommandTypes()) {
                String prettyList = ArrayUtils.makePrettyList(MayuCoreData.getMayuCommandsWithType(commandType).toArray());
                if (!prettyList.equals("")) {
                    embedBuilder.addField(StringUtils.prettyString(commandType.getName()), prettyList, false);
                }
            }
        }

        if (canSend) {
            event.reply(embedBuilder.build());
        } else {
            MessageUtils.send(MessageUtils.UnknownCommand.asEmbedBuilder(), event.getChannel());
        }
    }

    private boolean makeCommandSpecificHelp(EmbedBuilder embedBuilder, String commandName) {
        MayuCommand mayuCommand = MayuCoreData.getMayuCommand(commandName);

        if (mayuCommand == null) {
            return false;
        }

        embedBuilder.setTitle("Command ─ " + mayuCommand.getName());
        embedBuilder.setDescription(mayuCommand.description);

        embedBuilder.addField("Command Type", StringUtils.prettyString(mayuCommand.commandType.getName()), true);
        embedBuilder.addField("Syntax", "`" + mayuCommand.syntax + "`", false);

        String aliases = "```" + ArrayUtils.makeVerticalList(mayuCommand.getAliases()) + "```";
        if (aliases.equals("``````")) {
            aliases = "N/A";
        }
        String examples = "```" + ArrayUtils.makeVerticalList(mayuCommand.examples) + "```";
        if (examples.equals("``````")) {
            examples = "N/A";
        }
        embedBuilder.addField("Aliases", aliases, false);
        embedBuilder.addField("Examples", examples, false);

        String[] advancedInformation = new String[]{
                "Guild-Only...: " + mayuCommand.isGuildOnly(),
                "Cooldown.....: " + mayuCommand.getCooldown() + "s",
                "Required role: " + (mayuCommand.getRequiredRole() == null ? "N/A" : mayuCommand.getRequiredRole()),
                "Sub-commands.: " + ArrayUtils.makePrettyList(mayuCommand.getChildren())
        };
        embedBuilder.addField("Advanced", "```" + ArrayUtils.makeVerticalList(advancedInformation) + "```", false);

        return true;
    }
}
