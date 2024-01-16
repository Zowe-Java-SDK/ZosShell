package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.service.job.purge.PurgeService;

public class PurgeController {

    private static final Logger LOG = LoggerFactory.getLogger(PurgeController.class);

    private final PurgeService purgeService;

    public PurgeController(final PurgeService purgeService) {
        this.purgeService = purgeService;
    }

    public String purge(final String filter) {
        LOG.debug("*** purge ***");
        ResponseStatus responseStatus = purgeService.purge(filter);
        return responseStatus.getMessage();
    }

}
