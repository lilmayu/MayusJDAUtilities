package lilmayu.mayusjdautilities.utils;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;

public class ColorUtils {

    private static @Getter @Setter Color defaultColor = new Color(0xFF0087);

    private static @Getter @Setter Color errorColor = new Color(0xE04642);
    private static @Getter @Setter Color informationColor = new Color(0x4C95D8);
    private static @Getter @Setter Color warningColor = new Color(0xEBC730);
    private static @Getter @Setter Color successfulColor = new Color(0x42D074);
}
