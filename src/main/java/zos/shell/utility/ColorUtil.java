package zos.shell.utility;

import java.awt.*;

public final class ColorUtil {

    private ColorUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean validate(String colorName) {
        try {
            Color.class.getField(colorName.toUpperCase());
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
