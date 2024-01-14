package zos.shell.service.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.service.dsn.touch.TouchService;

import java.util.LinkedHashMap;
import java.util.Map;

public class EnvVariableService {

    private static final Logger LOG = LoggerFactory.getLogger(EnvVariableService.class);

    private static EnvVariableService INSTANCE;
    private final Map<String, String> variables = new LinkedHashMap<>();

    private EnvVariableService() {
        LOG.debug("*** EnvVariableService ***");
    }

    public static EnvVariableService getInstance() {
        LOG.debug("*** EnvVariableService ***");
        if (INSTANCE == null) {
            INSTANCE = new EnvVariableService();
        }
        return INSTANCE;
    }

    public String getValueByKeyName(final String key) {
        LOG.debug("*** getValueByKeyName ***");
        return variables.get(key.toUpperCase());
    }

    public Map<String, String> getVariables() {
        LOG.debug("*** getVariables ***");
        return variables;
    }

    public void setVariable(final String key, final String value) {
        LOG.debug("*** setVariable ***");
        variables.put(key.toUpperCase(), value.toUpperCase());
    }

}
