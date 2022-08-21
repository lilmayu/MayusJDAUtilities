package dev.mayuna.mayusjdautils.interactive.components;

import dev.mayuna.mayusjdautils.interactive.GroupedInteractionEvent;
import dev.mayuna.mayusjdautils.interactive.InteractiveListener;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.util.concurrent.TimeUnit;

public interface Interactable {

    boolean canInteract(User user);

    Pair<Long, TimeUnit> getExpireTime();

    boolean isExpired();

    void process(GroupedInteractionEvent interactionEvent);

    default void onExpire() {
        InteractiveListener.removeInteractable(this);
    }

    default void forceExpire() {
        onExpire();
    }
}
