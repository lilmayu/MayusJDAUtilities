package dev.mayuna.mayusjdautils.interactive.components;

import dev.mayuna.mayusjdautils.interactive.GroupedInteractionEvent;
import dev.mayuna.mayusjdautils.interactive.InteractiveListener;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;
import net.dv8tion.jda.internal.utils.tuple.ImmutablePair;
import net.dv8tion.jda.internal.utils.tuple.MutablePair;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class InteractiveModal implements Interactable {

    // Interaction
    private final @Getter Consumer<ModalInteractionEvent> modalClosedConsumer;

    // Settings
    private final @Getter Modal.Builder modalBuilder;
    // Other
    private final long createdTime = System.currentTimeMillis();
    private @Getter Pair<Long, TimeUnit> expireAfter = new MutablePair<>(5L, TimeUnit.MINUTES);
    private @Getter Runnable expiredRunnable;

    private InteractiveModal(String modalTitle, Consumer<Modal.Builder> modalBuilder, Consumer<ModalInteractionEvent> modalClosedConsumer) {
        this.modalBuilder = Modal.create(UUID.randomUUID().toString(), modalTitle);
        modalBuilder.accept(this.modalBuilder);
        this.modalClosedConsumer = modalClosedConsumer;
    }

    private InteractiveModal(Modal.Builder modalBuilder, Consumer<ModalInteractionEvent> modalClosedConsumer) {
        this.modalBuilder = modalBuilder;
        this.modalClosedConsumer = modalClosedConsumer;
    }

    private InteractiveModal(Modal.Builder modalBuilder) {
        this(modalBuilder, modalInteractionEvent -> {
            // Empty
        });
    }

    /**
     * Creates new {@link InteractiveModal} with {@link Modal.Builder} and {@link Consumer} with {@link ModalInteractionEvent} which is called when
     * the modal window is closed<br>The {@link Modal.Builder} will have random UUID as ID upon replying - your ID will be replaced by it.
     *
     * @param modalBuilder  Non-null {@link Modal.Builder}
     * @param onModalClosed Non-null {@link Consumer} with {@link ModalInteractionEvent}
     *
     * @return Non-null {@link InteractiveModal}
     *
     * @deprecated Use {@link #createTitled(String, Consumer, Consumer)} instead
     */
    @Deprecated
    public static @NonNull InteractiveModal create(@NonNull Modal.Builder modalBuilder, @NonNull Consumer<ModalInteractionEvent> onModalClosed) {
        return new InteractiveModal(modalBuilder, onModalClosed);
    }

    /**
     * Creates new {@link InteractiveModal} with specified title and {@link Consumer} with {@link ModalInteractionEvent} which is called when
     * the modal window is closed
     *
     * @param title         Non-null title
     * @param modalBuilder  Non-null {@link Consumer} with {@link Modal.Builder}
     * @param onModalClosed Non-null {@link Consumer} with {@link ModalInteractionEvent}
     *
     * @return Non-null {@link InteractiveModal}
     */
    public static @NonNull InteractiveModal createTitled(@NonNull String title, @NonNull Consumer<Modal.Builder> modalBuilder, @NonNull Consumer<ModalInteractionEvent> onModalClosed) {
        return new InteractiveModal(title, modalBuilder, onModalClosed);
    }

    /**
     * Replies to {@link IModalCallback} with this interactive modal. {@link IModalCallback} is for example {@link SlashCommandInteraction} or
     * {@link ButtonInteractionEvent}
     *
     * @param modalCallback Non-null {@link IModalCallback}
     *
     * @return Non-null {@link ModalCallbackAction}
     */
    public ModalCallbackAction replyModal(@NonNull IModalCallback modalCallback) {
        modalBuilder.setId(UUID.randomUUID().toString());
        InteractiveListener.addInteractable(this);
        return modalCallback.replyModal(modalBuilder.build());
    }

    ////////////
    // Others //
    ////////////

    public void expireAfter(long number, @NonNull TimeUnit timeUnit) {
        expireAfter = new ImmutablePair<>(number, timeUnit);
    }

    public void whenExpired(@NonNull Runnable expiredRunnable) {
        this.expiredRunnable = expiredRunnable;
    }

    ///////////////
    // Overrides //
    ///////////////

    @Override
    public void process(GroupedInteractionEvent event) {
        if (!event.isModalInteraction()) {
            return;
        }

        ModalInteractionEvent modalInteractionEvent = event.getModalInteractionEvent();

        if (!modalBuilder.getId().equals(modalInteractionEvent.getModalId())) {
            return;
        }

        modalClosedConsumer.accept(modalInteractionEvent);
    }

    @Override
    public boolean canInteract(@NonNull User user) {
        return true;
    }

    @Override
    public Pair<Long, TimeUnit> getExpireTime() {
        return expireAfter;
    }

    @Override
    public boolean isExpired() {
        long expireAfterMillis = expireAfter.getRight().toMillis(expireAfter.getLeft());

        if (expireAfterMillis == 0) {
            return false;
        }

        return (createdTime + expireAfterMillis) < System.currentTimeMillis();
    }

    @Override
    public void onExpire() {
        if (expiredRunnable != null) {
            expiredRunnable.run();
        }

        Interactable.super.onExpire();
    }
}
