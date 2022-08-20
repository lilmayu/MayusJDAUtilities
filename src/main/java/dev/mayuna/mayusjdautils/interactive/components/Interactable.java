package dev.mayuna.mayusjdautils.interactive.components;

import dev.mayuna.mayusjdautils.interactive.GroupedInteractionEvent;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.util.concurrent.TimeUnit;

public interface Interactable {

    boolean canInteract(User user);

    Pair<Long, TimeUnit> getExpireTime();

    void process(GroupedInteractionEvent interactionEvent);

    default void onExpire() {
        // TODO: Remove z listu
    }
}
