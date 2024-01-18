package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.service.console.ConsoleService;

public class ConsoleController {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleController.class);

    private final ConsoleService consoleService;

    public ConsoleController(final ConsoleService consoleService) {
        LOG.debug("*** ConsoleController ***");
        this.consoleService = consoleService;
    }

    public String issueConsole(final String command) {
        LOG.debug("*** issueConsole ***");
        ResponseStatus responseStatus = consoleService.issueConsole(command);
        return responseStatus.getMessage();
    }

}
