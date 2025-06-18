package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.dependency.DependencyController;
import zos.shell.response.ResponseStatus;
import zos.shell.service.console.ConsoleService;

public class ConsoleController extends DependencyController {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleController.class);

    private final ConsoleService consoleService;
    private final EnvVariableController envVariableController;

    public ConsoleController(final ConsoleService consoleService, final EnvVariableController envVariableController,
                             final Dependency dependency) {
        super(dependency);
        LOG.debug("*** ConsoleController ***");
        this.consoleService = consoleService;
        this.envVariableController = envVariableController;
    }

    public String issueConsole(final String command) {
        LOG.debug("*** issueConsole ***");
        String consoleName = envVariableController.getValueByEnv("CONSOLE_NAME");
        ResponseStatus responseStatus = consoleService.issueConsole(consoleName, command);
        return responseStatus.getMessage();
    }

}
