package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.service.env.EnvVariableService;

import java.util.TreeMap;

public class EnvVariableController {

    private static final Logger LOG = LoggerFactory.getLogger(EnvVariableController.class);
    private static final String ENV_SEPARATOR = "=";

    private final EnvVariableService envVariableService;

    public EnvVariableController(final EnvVariableService envVariableService) {
        LOG.debug("*** EnvVariableController ***");
        this.envVariableService = envVariableService;
    }

    public String env() {
        LOG.debug("*** env ***");
        var variables = envVariableService.getEnvVariables();

        var str = new StringBuilder();
        new TreeMap<>(variables).forEach((key, value) -> str.append(key)
                .append(ENV_SEPARATOR)
                .append(value)
                .append(System.lineSeparator()
                )
        );

        return str.toString();
    }

    public String set(final String key, final String value) {
        LOG.debug("*** set ***");
        var normalizedKey = key.trim().toUpperCase();
        var normalizedValue = value.trim();
        envVariableService.setEnvVariable(normalizedKey, normalizedValue);
        return String.format("%s%s%s", key, ENV_SEPARATOR, value);
    }

    public String getValueByEnv(final String key) {
        LOG.debug("*** getValueByEnv ***");
        return envVariableService.getValueByEnvName(key);
    }

}
