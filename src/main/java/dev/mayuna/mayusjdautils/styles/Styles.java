package dev.mayuna.mayusjdautils.styles;

import org.jetbrains.annotations.NotNull;

public interface Styles {

    /**
     * Copies styles from another styles object
     * @param styles Not-null styles
     */
    void copyFrom(@NotNull Styles styles);

}
