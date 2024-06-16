package dev.mayuna.mayusjdautils.interactive;

import dev.mayuna.mayusjdautils.interactive.components.Interactable;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Register this class into your JDA/ShardManager to ensure everything related to interactive in this library will work.
 */
public class InteractiveListener extends ListenerAdapter {

    private final static Object lock = new Object();

    private final static List<Interactable> interactables = Collections.synchronizedList(new LinkedList<>());
    private final static Timer expireCheckerTimer = new Timer("Interactable-Expire-Checker");
    private Executor eventProcessorExecutor = Executors.newCachedThreadPool();

    /**
     * Creates new instance of {@link InteractiveListener}
     */
    public InteractiveListener() {
        expireCheckerTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                interactables.removeIf(interactable -> {
                    if (interactable.isExpired()) {
                        interactable.onExpire();
                        return true;
                    } else {
                        return false;
                    }
                });
            }
        }, 0, 1000);
    }

    /**
     * Adds interactable to the list
     *
     * @param interactable {@link Interactable}
     */
    public static void addInteractable(Interactable interactable) {
        interactables.add(interactable);
    }

    /**
     * Removes interactable from the list
     *
     * @param interactable {@link Interactable}
     */
    public static void removeInteractable(Interactable interactable) {
        interactables.remove(interactable);
    }

    /**
     * Sets {@link Executor} for event processing
     *
     * @param eventProcessorExecutor {@link Executor}
     */
    public void setEventProcessorExecutor(@NonNull Executor eventProcessorExecutor) {
        this.eventProcessorExecutor = eventProcessorExecutor;
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
        interactables.forEach(interactable -> eventProcessorExecutor.execute(() -> interactable.process(interactionEvent)));
    }

    //////////
    // Util //
    //////////

    private boolean ensureUserIsValidAndNotBot(User user) {
        return user != null && !user.isBot();
    }
}
