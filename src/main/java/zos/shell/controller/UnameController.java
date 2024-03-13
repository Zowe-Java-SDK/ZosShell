package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.service.uname.UnameService;
import zos.shell.singleton.configuration.ConfigSingleton;
import zowe.client.sdk.core.ZosConnection;

public class UnameController {

    private static final Logger LOG = LoggerFactory.getLogger(UnameController.class);

    private final UnameService unameService;
    private final ConfigSingleton configSingleton;
    private final EnvVariableController envVariableController;

    public UnameController(final UnameService unameService, final ConfigSingleton configSingleton,
                           final EnvVariableController envVariableController) {
        LOG.debug("*** UnameController ***");
        this.unameService = unameService;
        this.configSingleton = configSingleton;
        this.envVariableController = envVariableController;
    }

    public String uname(final ZosConnection connection) {
        LOG.debug("*** uname ***");
        String consoleName = envVariableController.getValueByEnv("CONSOLE_NAME").trim();
        if (consoleName.isBlank()) {
            consoleName = configSingleton.getConfigSettings().getConsoleName();
        }
        return unameService.getUname(connection.getHost(), consoleName);
    }

}
