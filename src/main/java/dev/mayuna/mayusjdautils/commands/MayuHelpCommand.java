package dev.mayuna.mayusjdautils.commands;

import dev.mayuna.mayusjdautils.commands.types.BaseCommandType;
import dev.mayuna.mayusjdautils.commands.types.GeneralCommandType;
import dev.mayuna.mayusjdautils.data.MayuCoreData;
import dev.mayuna.mayusjdautils.lang.LanguageSettings;
import dev.mayuna.mayusjdautils.utils.ColorUtils;
import dev.mayuna.mayusjdautils.utils.DiscordUtils;
import dev.mayuna.mayusjdautils.utils.MessageInfo;
import dev.mayuna.mayuslibrary.utils.ArrayUtils;
import dev.mayuna.mayuslibrary.utils.StringUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class MayuHelpCommand extends MayuCommand {

    // -- Constructs -- //

    public MayuHelpCommand() {
        this.name = "help";
        this.description = LanguageSettings.HelpCommand.getCommandDescription();
        this.syntax = "help [command]";
        this.examples = new String[]{"help", "help ping"};
        this.commandType = new GeneralCommandType();

        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(OptionType.STRING, "specific_command", "View command specific help!").setRequired(false));
        this.options = options;

        this.guildOnly = false;
    }

    @Override
    protected void execute(SlashCommandEvent event) {
        event.deferReply(true).complete();

        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();
        embedBuilder.setTitle(LanguageSettings.HelpCommand.getTitle());
        embedBuilder.setDescription(LanguageSettings.HelpCommand.getEmbedHelpCommandHomeDescription());
        embedBuilder.setColor(ColorUtils.getDefaultColor());

        boolean canSend = true;

        OptionMapping option = event.getOption("specific_command");

        if (option != null) {
            String commandName = option.getAsString();
            canSend = makeCommandSpecificHelp(embedBuilder, commandName);
        } else {
            for (BaseCommandType commandType : MayuCoreData.getCOMMAND_TYPES()) {
                String prettyList = ArrayUtils.makePrettyList(MayuCoreData.getCommandsWithType(commandType).toArray());
                if (!prettyList.equals("")) {
                    embedBuilder.addField(StringUtils.prettyString(commandType.getName()), prettyList, false);
                }
            }
        }

        if (canSend) {
            event.getHook().editOriginalEmbeds(embedBuilder.build()).queue();
        } else {
            event.getChannel().sendMessageEmbeds(MessageInfo.UnknownCommand.asEmbedBuilder().build()).complete();
        }
    }

    private boolean makeCommandSpecificHelp(EmbedBuilder embedBuilder, String commandName) {
        MayuCommand mayuCommand = MayuCoreData.getCommand(commandName);

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

        String[] advancedInformation = new String[]{"Guild-Only...: " + mayuCommand.isGuildOnly(), "Cool-down.....: " + mayuCommand.getCooldown() + "s", "Required role: " + (mayuCommand.getRequiredRole() == null ? "N/A" : mayuCommand.getRequiredRole()), "Sub-commands.: " + ArrayUtils.makePrettyList(
                mayuCommand.getChildren())};
        embedBuilder.addField("Advanced", "```" + ArrayUtils.makeVerticalList(advancedInformation) + "```", false);

        return true;
    }
}
