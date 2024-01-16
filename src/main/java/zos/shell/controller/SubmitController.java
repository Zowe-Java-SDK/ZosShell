package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.service.job.submit.SubmitService;

public class SubmitController {

    private static final Logger LOG = LoggerFactory.getLogger(SubmitController.class);

    private final SubmitService submitService;

    public SubmitController(final SubmitService submitService) {
        LOG.debug("*** SubmitController ***");
        this.submitService = submitService;
    }

    public String submit(final String dataset, final String target) {
        LOG.debug("*** submit ***");
        ResponseStatus responseStatus = submitService.submit(dataset, target);
        return responseStatus.getMessage();
    }

}
