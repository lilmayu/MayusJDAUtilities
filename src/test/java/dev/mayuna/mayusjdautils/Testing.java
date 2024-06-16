package dev.mayuna.mayusjdautils;

import dev.mayuna.mayusjdautils.interactive.Interaction;
import dev.mayuna.mayusjdautils.interactive.components.InteractiveMessage;
import dev.mayuna.mayusjdautils.interactive.components.InteractiveModal;
import dev.mayuna.mayusjdautils.interactive.components.InteractiveRowedMessage;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.EnumSet;

public class Testing {

    public void test() {
        InteractiveMessage interactiveMessage = InteractiveMessage.createEmpty();
        interactiveMessage.addInteraction(Interaction.asButton(ButtonStyle.LINK, "xd"), event -> {

        });
    }

    public void rowed() {
        InteractiveRowedMessage.builder().addStringSelectMenu(0, "Please, select something.", selectMenuBuilder -> {
            selectMenuBuilder.addOption("Something", "something");
            selectMenuBuilder.setDefaultValues("something");
            selectMenuBuilder.setMaxValues(2);
        }, event -> {
            System.out.println(event.getInteraction().getSelectedOptions());
        });

        InteractiveRowedMessage.builder().addStringSelectMenu(0, "Please, select something.")
                               .onInteraction(0, Interaction.asSelectOption("Okay", "xd"), event -> {
                                   System.out.println("Selected okay");
                               });

        InteractiveRowedMessage.builder()
                               .addEntitySelectMenu(0, "Please, select something.", EnumSet.of(EntitySelectMenu.SelectTarget.USER, EntitySelectMenu.SelectTarget.ROLE), selectMenuBuilder -> {
                                   selectMenuBuilder.setMaxValues(2);
                               }, event -> {
                                   System.out.println(event.getInteraction().getMentions());
                               });
    }

    public void modal() {
        InteractiveModal.createTitled("sOME GOOD MODAL", builder -> {
            builder.addActionRow(TextInput.create("okay", "good", TextInputStyle.SHORT).build());
        }, event -> {

        });
    }

}
