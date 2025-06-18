package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.service.env.EnvVariableService;

import java.util.TreeMap;

public class EnvVariableController {

    private static final Logger LOG = LoggerFactory.getLogger(EnvVariableController.class);
    private static final String NO_ENV_VARIABLES_MESSAGE = "No environment variables set, try again...";
    private static final String ENV_SEPARATOR = "=";

    private final EnvVariableService envVariableService;

    public EnvVariableController(final EnvVariableService envVariableService) {
        LOG.debug("*** EnvVariableController ***");
        this.envVariableService = envVariableService;
    }

    public String env() {
        LOG.debug("*** env ***");
        var variables = envVariableService.getEnvVariables();

        if (variables.isEmpty()) {
            return NO_ENV_VARIABLES_MESSAGE;
        }

        var str = new StringBuilder();
        new TreeMap<>(variables)
                .forEach((key, value) ->
                        str.append(String.format("%s%s%s%n", key, ENV_SEPARATOR, value)));
        return str.toString();
    }

    public String set(final String keyValue) {
        LOG.debug("*** set ***");

        if (keyValue == null || keyValue.isBlank()) {
            return Constants.INVALID_COMMAND;
        }

        var values = keyValue.split(ENV_SEPARATOR, 2);
        if (values.length != 2) {
            return Constants.INVALID_COMMAND;
        }

        String key = values[0].trim();
        String value = values[1].trim();

        envVariableService.setEnvVariable(key, value);
        return String.format("%s%s%s", key, ENV_SEPARATOR, value);
    }

    public String getValueByEnv(final String key) {
        LOG.debug("*** getValueByEnv ***");
        return envVariableService.getValueByEnvName(key);
    }

}
