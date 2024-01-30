package zos.shell.service.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.singleton.EnvVariableSingleton;

import java.util.Map;

public class EnvVariableService {

    private static final Logger LOG = LoggerFactory.getLogger(EnvVariableService.class);

    private static final EnvVariableSingleton INSTANCE = EnvVariableSingleton.getInstance();

    public EnvVariableService() {
        LOG.debug("*** EnvVariableService ***");
    }

    public String getValueByEnvName(final String key) {
        LOG.debug("*** getValueByEnvName ***");
        return INSTANCE.getVariables().get(key.toUpperCase());
    }

    public Map<String, String> getEnvVariables() {
        LOG.debug("*** getEnvVariables ***");
        return INSTANCE.getVariables();
    }

    public void setEnvVariable(final String key, final String value) {
        LOG.debug("*** setEnvVariable ***");
        INSTANCE.getVariables().put(key.toUpperCase(), value.toUpperCase());
    }

}
