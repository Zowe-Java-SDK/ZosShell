package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.service.change.ChangeConnService;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;

public class ChangeConnController {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeConnController.class);

    private final ChangeConnService changeConnService;

    public ChangeConnController(final ChangeConnService changeConnService) {
        LOG.debug("*** ChangeZosController ***");
        this.changeConnService = changeConnService;
    }

    public ZosConnection changeZosConnection(final ZosConnection connection, final String[] commands) {
        LOG.debug("*** changeZosConnection ***");
        return changeConnService.changeZosConnection(connection, commands);
    }

    public SshConnection changeSshConnection(final SshConnection connection, final String[] commands) {
        LOG.debug("*** changeSshConnection ***");
        return changeConnService.changeSshConnection(connection, commands);
    }

    public void displayConnections() {
        LOG.debug("*** displayConnections ***");
        changeConnService.displayConnections();
    }

}