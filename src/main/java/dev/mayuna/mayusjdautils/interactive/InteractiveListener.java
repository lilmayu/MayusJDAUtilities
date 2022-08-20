package dev.mayuna.mayusjdautils.interactive;

import dev.mayuna.mayusjdautils.interactive.components.Interactable;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Register this class into your JDA/ShardManager to ensure everything related to interactive in this library will work.
 */
public class InteractiveListener extends ListenerAdapter {

    private final static List<Interactable> interactables = Collections.synchronizedList(new LinkedList<>());

    public InteractiveListener() {

    }

    /////////////////////
    // Event Listeners //
    /////////////////////

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (!ensureUserIsValidAndNotBot(event.getUser())) {
            return;
        }

        GroupedInteractionEvent interactionEvent = new GroupedInteractionEvent(event);
        processEvent(interactionEvent);
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        if (!ensureUserIsValidAndNotBot(event.getUser())) {
            return;
        }

        GroupedInteractionEvent interactionEvent = new GroupedInteractionEvent(event);
        processEvent(interactionEvent);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!ensureUserIsValidAndNotBot(event.getUser())) {
            return;
        }

        GroupedInteractionEvent interactionEvent = new GroupedInteractionEvent(event);
        processEvent(interactionEvent);
    }

    @Override
    public void onSelectMenuInteraction(SelectMenuInteractionEvent event) {
        if (!ensureUserIsValidAndNotBot(event.getUser())) {
            return;
        }

        GroupedInteractionEvent interactionEvent = new GroupedInteractionEvent(event);
        processEvent(interactionEvent);
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (!ensureUserIsValidAndNotBot(event.getUser())) {
            return;
        }

        GroupedInteractionEvent interactionEvent = new GroupedInteractionEvent(event);
        processEvent(interactionEvent);
    }

    ////////////////
    // Processing //
    ////////////////

    private void processEvent(GroupedInteractionEvent interactionEvent) {
        interactables.forEach(interactable -> interactable.process(interactionEvent));
    }

    /////////
    // API //
    /////////

    public static void addInteractable(Interactable interactable) {
        interactables.add(interactable);
    }

    //////////
    // Util //
    //////////

    private boolean ensureUserIsValidAndNotBot(User user) {
        return user != null && !user.isBot();
    }
}
