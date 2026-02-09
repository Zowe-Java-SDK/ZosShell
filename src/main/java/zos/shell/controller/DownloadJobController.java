package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.dependency.DependencyController;
import zos.shell.response.ResponseStatus;
import zos.shell.service.job.download.DownloadJobService;

public class DownloadJobController extends DependencyController {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadJobController.class);

    private final DownloadJobService downloadJobService;

    public DownloadJobController(final DownloadJobService downloadJobService, final Dependency dependency) {
        super(dependency);
        LOG.debug("*** DownloadJobController ***");
        this.downloadJobService = downloadJobService;
    }

    public String downloadJob(final String target, final String jobid) {
        LOG.debug("*** downloadJob ***");
        ResponseStatus responseStatus = downloadJobService.download(target, jobid);
        return responseStatus.getMessage();
    }

}
