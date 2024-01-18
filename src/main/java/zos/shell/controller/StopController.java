package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.service.job.terminate.TerminateService;

public class StopController {

    private static final Logger LOG = LoggerFactory.getLogger(StopController.class);

    private final TerminateService terminateService;

    public StopController(final TerminateService terminateService) {
        LOG.debug("*** TerminateController ***");
        this.terminateService = terminateService;
    }

    public String stop(final String target) {
        LOG.debug("*** stop ***");
        ResponseStatus responseStatus = terminateService.terminate(TerminateService.Type.STOP, target);
        return responseStatus.getMessage();
    }

}
