package zos.shell.utility;

import java.awt.*;
import java.lang.reflect.Field;

public final class ColorUtil {

    private ColorUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static void validate(String colorName) {
        if (colorName == null) {
            return;
        }
        try {
            Field field = Color.class.getField(colorName.toLowerCase());
            field.get(null); // access static field
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid color: " + colorName);
        }
    }

}
