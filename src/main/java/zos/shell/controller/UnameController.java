package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.console.ConsoleService;
import zowe.client.sdk.core.ZosConnection;

public class UnameController {

    private static final Logger LOG = LoggerFactory.getLogger(UnameController.class);

    private final ConsoleService consoleService;

    public UnameController(final ConsoleService consoleService) {
        LOG.debug("*** UnameController ***");
        this.consoleService = consoleService;
    }

    public String uname(final ZosConnection connection) {
        LOG.debug("*** uname ***");
        ResponseStatus response = consoleService.issueConsole("D IPLINFO");
        if (!response.isStatus()) {
            return Constants.NO_INFO;
        }
        String output = response.getMessage();
        final var index = output.indexOf("RELEASE z/OS ");
        String zosVersion = null;
        if (index >= 0) {
            zosVersion = output.substring(index, index + 22);
        }
        if (zosVersion == null) {
            return Constants.NO_INFO;
        }
        return "hostname: " + connection.getHost() + ", " + zosVersion;
    }

}
