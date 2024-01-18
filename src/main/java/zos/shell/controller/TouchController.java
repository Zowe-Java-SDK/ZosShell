package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.touch.TouchService;

public class TouchController {

    private static final Logger LOG = LoggerFactory.getLogger(TouchController.class);

    private final TouchService touchService;

    public TouchController(final TouchService touchService) {
        LOG.debug("*** TouchController ***");
        this.touchService = touchService;
    }

    public String touch(final String dataset, final String target) {
        LOG.debug("*** touch ***");
        ResponseStatus responseStatus = touchService.touch(dataset, target);
        return responseStatus.getMessage();
    }

}
