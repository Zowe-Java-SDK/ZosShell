package zos.shell.singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EnvVariableSingleton {

    private static final Logger LOG = LoggerFactory.getLogger(EnvVariableSingleton.class);

    private static class Holder {
        private static final EnvVariableSingleton INSTANCE = new EnvVariableSingleton();
    }

    private final Map<String, String> variables = new LinkedHashMap<>();

    private EnvVariableSingleton() {
        LOG.debug("*** EnvVariableSingleton ***");
    }

    public static EnvVariableSingleton getInstance() {
        LOG.debug("*** getInstance ***");
        return Holder.INSTANCE;
    }

    public synchronized void set(final String key, final String value) {
        LOG.debug("*** set ***");
        if (key == null) return;
        variables.put(key, value);
    }

    public synchronized String get(final String key) {
        LOG.debug("*** get ***");
        return variables.get(key);
    }

    public synchronized void remove(final String key) {
        LOG.debug("*** remove ***");
        variables.remove(key);
    }

    public synchronized Map<String, String> getAll() {
        LOG.debug("*** getAll ***");
        return new LinkedHashMap<>(variables); // defensive copy
    }

}
