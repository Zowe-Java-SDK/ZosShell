package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.service.console.ConsoleService;
import zos.shell.singleton.configuration.ConfigSingleton;

public class ConsoleController {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleController.class);

    private final ConsoleService consoleService;
    private final ConfigSingleton configSingleton;
    private final EnvVariableController envVariableController;

    public ConsoleController(final ConsoleService consoleService, final ConfigSingleton configSingleton,
                             final EnvVariableController envVariableController) {
        LOG.debug("*** ConsoleController ***");
        this.consoleService = consoleService;
        this.configSingleton = configSingleton;
        this.envVariableController = envVariableController;
    }

    public String issueConsole(final String command) {
        LOG.debug("*** issueConsole ***");
        String consoleName = envVariableController.getValueByEnv("CONSOLE_NAME").trim();
        if (consoleName.isBlank()) {
            consoleName = configSingleton.getConfigSettings().getConsoleName();
        }
        ResponseStatus responseStatus = consoleService.issueConsole(consoleName, command);
        return responseStatus.getMessage();
    }

}
