package zos.shell.configuration;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.utility.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MvsConsoles {

    private static final Logger LOG = LoggerFactory.getLogger(MvsConsoles.class);

    private final Map<String, String> consoles = new HashMap<>();

    public MvsConsoles() {
        LOG.debug("*** MvsConsoles ***");
        setup();
    }

    public void setup() {
        LOG.debug("*** setup ***");
        File file = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            file = new File(Constants.SECURITY_CONFIG_PATH_FILE_WINDOWS);
        } else if (SystemUtils.IS_OS_MAC) {
            file = new File(Constants.SECURITY_CONFIG_PATH_FILE_MAC);
        }
        try (final var br = new BufferedReader(new FileReader(Objects.requireNonNull(file)))) {
            String str;
            while ((str = br.readLine()) != null) {
                final var items = str.split(",");
                if (items.length >= 5 && !Util.isStrNum(items[4])) {
                    consoles.put(items[0], items[4]);
                }
            }
        } catch (IOException | NullPointerException ignored) {
        }
    }

    public String getConsoleName(String connection) {
        LOG.debug("*** getConsoleName ***");
        return consoles.get(connection);
    }

}
