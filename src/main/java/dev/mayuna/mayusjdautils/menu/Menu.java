package dev.mayuna.mayusjdautils.menu;

import dev.mayuna.mayusjdautils.interactive.components.InteractiveRowedMessage;
import lombok.NonNull;

/**
 * Simple class to create basic template for menus
 */
public abstract class Menu {

    /**
     * Creates a new {@link InteractiveRowedMessage}
     *
     * @return {@link InteractiveRowedMessage}
     */
    protected abstract @NonNull InteractiveRowedMessage create();
}
