package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.job.browse.BrowseLogService;

public class BrowseJobController {

    private static final Logger LOG = LoggerFactory.getLogger(BrowseJobController.class);

    private final BrowseLogService browseLogService;

    public BrowseJobController(final BrowseLogService browseLogService) {
        LOG.debug("*** BrowseJobController ***");
        this.browseLogService = browseLogService;
    }

    public String browseJob(final String target) {
        LOG.debug("*** browseJob ***");
        ResponseStatus responseStatus = browseLogService.browseJob(target);
        if (responseStatus.getMessage().split("\\n").length > Constants.BROWSE_LIMIT) {
            return Constants.BROWSE_LIMIT_WARNING;
        }
        return responseStatus.getMessage();
    }

}
