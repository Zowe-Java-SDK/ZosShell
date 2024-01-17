package zos.shell.environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.service.env.EnvVariableService;

import java.util.LinkedHashMap;
import java.util.Map;

public class EnvVariableSingleton {

    private static final Logger LOG = LoggerFactory.getLogger(EnvVariableService.class);

    private static class Holder {
        private static final EnvVariableSingleton instance = new EnvVariableSingleton();
    }

    private final Map<String, String> variables = new LinkedHashMap<>();

    private EnvVariableSingleton() {
        LOG.debug("*** EnvVariableSingleton ***");
    }

    public static EnvVariableSingleton getInstance() {
        LOG.debug("*** getInstance ***");
        return EnvVariableSingleton.Holder.instance;
    }

    public Map<String, String> getVariables() {
        LOG.debug("*** getVariables ***");
        return variables;
    }

}
