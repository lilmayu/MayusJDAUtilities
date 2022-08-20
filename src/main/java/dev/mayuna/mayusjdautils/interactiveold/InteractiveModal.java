package dev.mayuna.mayusjdautils.interactiveold;

import dev.mayuna.mayusjdautils.data.MayuCoreListener;
import lombok.Getter;
import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;

import java.util.UUID;
import java.util.function.Consumer;

public class InteractiveModal {

    private final @Getter Modal.Builder modalBuilder;
    private final @Getter Consumer<ModalInteractionEvent> onModalClosed;

    private InteractiveModal(Modal.Builder modalBuilder, Consumer<ModalInteractionEvent> onModalClosed) {
        this.modalBuilder = modalBuilder;
        this.onModalClosed = onModalClosed;
    }

    private InteractiveModal(Modal.Builder modalBuilder) {
        this(modalBuilder, modalInteractionEvent -> {});
    }

    /**
     * Creates new {@link InteractiveModal} with {@link Modal.Builder} and {@link Consumer} with {@link ModalInteractionEvent} which is called when the modal window is closed
     *
     * @param modalBuilder  Non-null {@link Modal.Builder}
     * @param onModalClosed Non-null {@link Consumer} with {@link ModalInteractionEvent}
     *
     * @return Non-null {@link InteractiveModal}
     */
    public static @NonNull InteractiveModal create(@NonNull Modal.Builder modalBuilder, @NonNull Consumer<ModalInteractionEvent> onModalClosed) {
        return new InteractiveModal(modalBuilder, onModalClosed);
    }

    /**
     * Creates new {@link InteractiveModal} with {@link Modal.Builder} without any consumer which would be called when the modal window closes
     *
     * @param modalBuilder Non-null {@link Modal.Builder}
     *
     * @return Non-null {@link InteractiveModal}
     */
    public static @NonNull InteractiveModal create(@NonNull Modal.Builder modalBuilder) {
        return new InteractiveModal(modalBuilder);
    }

    public ModalCallbackAction reply(IModalCallback modalCallback) {
        modalBuilder.setId(UUID.randomUUID().toString());
        MayuCoreListener.addInteractiveModal(this);
        return modalCallback.replyModal(modalBuilder.build());
    }
}
