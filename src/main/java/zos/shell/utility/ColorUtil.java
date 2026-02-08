package zos.shell.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public final class ColorUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ColorUtil.class);

    private ColorUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean validate(String colorName) {
        LOG.debug("validate({})", colorName);
        try {
            Color.class.getField(colorName.toUpperCase());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
