package dev.mayuna.mayusjdautils.interactive;

import dev.mayuna.mayusjdautils.interactive.components.Interactable;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Register this class into your JDA/ShardManager to ensure everything related to interactive in this library will work.
 */
public class InteractiveListener extends ListenerAdapter {

    private final static List<Interactable> interactables = new CopyOnWriteArrayList<>();
    private final static Timer expireCheckerTimer = new Timer("INTERACTABLE-EXPIRE-CHECKER");

    public InteractiveListener() {
        expireCheckerTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                List<Interactable> toRemove = new LinkedList<>();

                interactables.forEach(interactable -> {
                    if (interactable.isExpired()) {
                        interactable.onExpire();
                        toRemove.add(interactable);
                    }
                });

                interactables.removeAll(toRemove);
            }
        }, 0, 1000);
    }

    /////////
    // API //
    /////////

    public static void addInteractable(Interactable interactable) {
        interactables.add(interactable);
    }

    public static void removeInteractable(Interactable interactable) {
        interactables.remove(interactable);
    }

    public static List<Interactable> getIntractables() {
        return Collections.unmodifiableList(interactables);
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
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!ensureUserIsValidAndNotBot(event.getUser())) {
            return;
        }

        GroupedInteractionEvent interactionEvent = new GroupedInteractionEvent(event);
        processEvent(interactionEvent);
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (!ensureUserIsValidAndNotBot(event.getUser())) {
            return;
        }

        GroupedInteractionEvent interactionEvent = new GroupedInteractionEvent(event);
        processEvent(interactionEvent);
    }

    @Override
    public void onEntitySelectInteraction(EntitySelectInteractionEvent event) {
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

    //////////
    // Util //
    //////////

    private boolean ensureUserIsValidAndNotBot(User user) {
        return user != null && !user.isBot();
    }
}
