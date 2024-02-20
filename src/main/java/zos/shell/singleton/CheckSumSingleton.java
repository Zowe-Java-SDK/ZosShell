package zos.shell.singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CheckSumSingleton {

    private static final Logger LOG = LoggerFactory.getLogger(CheckSumSingleton.class);

    public static Map<String, String> checksums = new HashMap<>();

    private static class Holder {
        private static final CheckSumSingleton instance = new CheckSumSingleton();
    }

    private CheckSumSingleton() {
        LOG.debug("*** CheckSumSingleton ***");
    }

    public static CheckSumSingleton getInstance() {
        LOG.debug("*** getInstance ***");
        return CheckSumSingleton.Holder.instance;
    }

    public String get(final String target) {
        LOG.debug("*** get ***");
        return checksums.get(target);
    }

    public void put(final String target, final String value) {
        LOG.debug("*** put ***");
        checksums.put(target, value);
    }

}
