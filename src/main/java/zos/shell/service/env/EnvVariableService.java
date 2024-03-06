package zos.shell.service.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.singleton.EnvVariableSingleton;

import java.util.Map;
import java.util.regex.Pattern;

public class EnvVariableService {

    private static final Logger LOG = LoggerFactory.getLogger(EnvVariableService.class);

    private static final EnvVariableSingleton envVariableSingleton = EnvVariableSingleton.getInstance();

    public EnvVariableService() {
        LOG.debug("*** EnvVariableService ***");
    }

    public String getValueByEnvName(final String key) {
        LOG.debug("*** getValueByEnvName ***");
        return envVariableSingleton.getVariables().get(key.toUpperCase());
    }

    public Map<String, String> getEnvVariables() {
        LOG.debug("*** getEnvVariables ***");
        return envVariableSingleton.getVariables();
    }

    public void setEnvVariable(final String key, String value) {
        LOG.debug("*** setEnvVariable ***");
        var p = Pattern.compile("\"([^\"]*)\"");
        var m = p.matcher(value);
        while (m.find()) {
            value = m.group(1);
        }
        envVariableSingleton.getVariables().put(key.toUpperCase(), value.toUpperCase());
    }

}
