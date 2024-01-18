package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.service.job.terminate.TerminateService;

public class CancelController {

    private static final Logger LOG = LoggerFactory.getLogger(CancelController.class);

    private final TerminateService terminateService;

    public CancelController(final TerminateService terminateService) {
        LOG.debug("*** CancelService ***");
        this.terminateService = terminateService;
    }

    public String cancel(final String target) {
        LOG.debug("*** cancel ***");
        ResponseStatus responseStatus = terminateService.terminate(TerminateService.Type.CANCEL, target);
        return responseStatus.getMessage();
    }

}
