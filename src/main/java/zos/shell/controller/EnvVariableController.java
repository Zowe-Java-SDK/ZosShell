package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.service.env.EnvVariableService;
import zos.shell.utility.StrUtil;

import java.util.TreeMap;

public class EnvVariableController {

    private static final Logger LOG = LoggerFactory.getLogger(EnvVariableController.class);

    private final EnvVariableService envVariableService;

    public EnvVariableController(final EnvVariableService envVariableService) {
        LOG.debug("*** EnvVariableController ***");
        this.envVariableService = envVariableService;
    }

    public String env() {
        LOG.debug("*** env ***");
        if (envVariableService.getEnvVariables().isEmpty()) {
            return "no environment variables set, try again...";
        }
        var str = new StringBuilder();
        new TreeMap<>(envVariableService.getEnvVariables())
                .forEach((k, v) -> str.append(k).append("=").append(v).append("\n"));
        return str.toString();
    }

    public String set(final String key_value) {
        LOG.debug("*** set ***");
        var values = key_value.split("=");
        if (values.length != 2) {
            return Constants.INVALID_COMMAND;
        }
        envVariableService.setEnvVariable(values[0].trim(), values[1].trim());
        if (values[0].equalsIgnoreCase("acctnum")) {
            if (!StrUtil.isStrNum(values[1])) {
                return "ACCTNUM value not a number, try again...";
            }
        }
        return values[0] + "=" + values[1];
    }

    public String getValueByEnv(final String key) {
        LOG.debug("*** getValueByEnv ***");
        return envVariableService.getValueByEnvName(key);
    }

}
