package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.dependency.DependencyController;
import zos.shell.response.ResponseStatus;
import zos.shell.service.job.browse.BrowseLogService;

public class BrowseJobController extends DependencyController {

    private static final Logger LOG = LoggerFactory.getLogger(BrowseJobController.class);

    private final BrowseLogService browseLogService;
    private final EnvVariableController envVariableController;

    public BrowseJobController(final BrowseLogService browseLogService,
                               final EnvVariableController envVariableController,
                               final Dependency dependency) {
        super(dependency);
        LOG.debug("*** BrowseJobController ***");
        this.browseLogService = browseLogService;
        this.envVariableController = envVariableController;
    }

    public String browseJob(final String target) {
        return this.browseJob(target, null);
    }

    public String browseJob(final String target, String jobId) {
        LOG.debug("*** browseJob ***");
        ResponseStatus responseStatus = browseLogService.browseJob(target, jobId);
        String browseLimit = envVariableController.getValueByEnv("BROWSE_LIMIT");
        int browseLimitInt = 0;
        if (!browseLimit.isBlank()) {
            try {
                browseLimitInt = Integer.parseInt(browseLimit);
            } catch (NumberFormatException ignored) {
            }
        }
        if (responseStatus.getMessage().split("\\n").length >
                (browseLimitInt > 0 ? browseLimitInt : Constants.BROWSE_LIMIT)) {
            return Constants.BROWSE_LIMIT_WARNING;
        }
        return responseStatus.getMessage();
    }

}
