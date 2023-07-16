package dev.mayuna.mayusjdautils.styles;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ColorStyles implements Styles {

    private @Getter @Setter Color defaultColor = new Color(0xFF0087);

    private @Getter @Setter Color error = new Color(0xE04642);
    private @Getter @Setter Color information = new Color(0x4C95D8);
    private @Getter @Setter Color warning = new Color(0xEBC730);
    private @Getter @Setter Color success = new Color(0x42D074);

    @Override
    public void copyFrom(@NotNull Styles styles) {
        if (!(styles instanceof ColorStyles)) {
            return;
        }

        ColorStyles otherColorStyles = (ColorStyles) styles;

        defaultColor = otherColorStyles.defaultColor;
        error = otherColorStyles.error;
        information = otherColorStyles.information;
        warning = otherColorStyles.warning;
        success = otherColorStyles.success;
    }
}
