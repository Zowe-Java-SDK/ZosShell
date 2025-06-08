package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.dependency.DependencyController;
import zos.shell.response.ResponseStatus;
import zos.shell.service.job.processlst.ProcessLstService;

public class ProcessLstController extends DependencyController {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessLstController.class);

    private final ProcessLstService processListingService;

    public ProcessLstController(final ProcessLstService processListingService, final Dependency dependency) {
        super(dependency);
        this.processListingService = processListingService;
    }

    public String processList() {
        LOG.debug("*** processList all ***");
        return processList(null);
    }

    public String processList(final String target) {
        LOG.debug("*** processList target ***");
        ResponseStatus responseStatus = processListingService.processLst(target);
        return responseStatus.getMessage();
    }

}
