package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.service.echo.EchoService;

public class EchoController {

    private static final Logger LOG = LoggerFactory.getLogger(EchoController.class);

    private final EchoService echoService;

    public EchoController(final EchoService echoService) {
        LOG.debug("*** EchoController ***");
        this.echoService = echoService;
    }

    public String getEcho(final String arg) {
        LOG.debug("*** getEcho ***");
        return echoService.getEcho(arg);
    }

}
