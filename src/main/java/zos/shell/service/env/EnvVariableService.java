package zos.shell.service.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.singleton.EnvVariableSingleton;

import java.util.Map;

public class EnvVariableService {

    private static final Logger LOG = LoggerFactory.getLogger(EnvVariableService.class);

    private static final EnvVariableSingleton ENV_VARIABLE_SINGLETON = EnvVariableSingleton.getInstance();

    public EnvVariableService() {
        LOG.debug("*** EnvVariableService ***");
    }

    public String getValueByEnvName(final String key) {
        LOG.debug("*** getValueByEnvName ***");
        String value = ENV_VARIABLE_SINGLETON.get(key.toUpperCase());
        return value != null ? value : "";
    }

    public Map<String, String> getEnvVariables() {
        LOG.debug("*** getEnvVariables ***");
        return ENV_VARIABLE_SINGLETON.getAll();
    }

    public void setEnvVariable(final String key, String value) {
        LOG.debug("*** setEnvVariable ***");
        ENV_VARIABLE_SINGLETON.set(key.toUpperCase().trim(), value.trim());
    }

}
