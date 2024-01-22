package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.service.tso.TsoService;

public class TsoController {

    private static final Logger LOG = LoggerFactory.getLogger(TsoController.class);

    private final TsoService tsoService;

    public TsoController(final TsoService tsoService) {
        LOG.debug("*** TsoController ***");
        this.tsoService = tsoService;
    }

    public String issueCommand(final String command) {
        LOG.debug("*** issueCommand ***");
        ResponseStatus responseStatus = tsoService.issueCommand(command);
        return responseStatus.getMessage();
    }

}
