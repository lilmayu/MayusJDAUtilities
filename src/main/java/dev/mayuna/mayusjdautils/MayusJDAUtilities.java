package dev.mayuna.mayusjdautils;

import dev.mayuna.mayusjdautils.lang.LanguageSettings;
import dev.mayuna.mayusjdautils.styles.ColorStyles;
import dev.mayuna.mayusjdautils.styles.MessageInfoStyles;
import dev.mayuna.mayusjdautils.styles.Styles;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public class MayusJDAUtilities implements Styles {

    private @Getter @Setter ColorStyles colorStyles = new ColorStyles();
    private @Getter @Setter MessageInfoStyles messageInfoStyles = new MessageInfoStyles(this);
    private @Getter @Setter LanguageSettings languageSettings = new LanguageSettings();

    /**
     * {@inheritDoc}
     * @param styles Not-null styles
     */
    @Override
    public void copyFrom(@NotNull Styles styles) {
        if (!(styles instanceof MayusJDAUtilities)) {
            return;
        }

        MayusJDAUtilities otherMayusJDAUtilities = (MayusJDAUtilities) styles;

        colorStyles.copyFrom(otherMayusJDAUtilities.colorStyles);
        messageInfoStyles.copyFrom(otherMayusJDAUtilities.messageInfoStyles);
        languageSettings = otherMayusJDAUtilities.languageSettings;
    }
}
