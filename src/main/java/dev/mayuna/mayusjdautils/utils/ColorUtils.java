package dev.mayuna.mayusjdautils.utils;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;

public class ColorUtils {

    private static @Getter @Setter Color defaultColor = new Color(0xFF0087);

    private static @Getter @Setter Color error = new Color(0xE04642);
    private static @Getter @Setter Color information = new Color(0x4C95D8);
    private static @Getter @Setter Color warning = new Color(0xEBC730);
    private static @Getter @Setter Color success = new Color(0x42D074);
}
