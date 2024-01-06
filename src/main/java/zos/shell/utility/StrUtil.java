package zos.shell.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public final class StrUtil {

    private static final Logger LOG = LoggerFactory.getLogger(StrUtil.class);

    public static boolean isStrNum(String str) {
        LOG.debug("*** isStrNum ***");
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static String[] stripEmptyStrings(String[] command) {
        LOG.debug("*** stripEmptyStrings ***");
        final var list = new ArrayList<>(Arrays.asList(command));
        list.removeAll(Collections.singleton(""));
        command = list.toArray(new String[0]);
        return command;
    }

}
