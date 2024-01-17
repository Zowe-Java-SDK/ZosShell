package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.service.job.download.DownloadJobService;

public class DownloadJobController {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadJobController.class);

    private final DownloadJobService downloadJobService;

    public DownloadJobController(final DownloadJobService downloadJobService) {
        LOG.debug("*** DownloadJobController ***");
        this.downloadJobService = downloadJobService;
    }

    public String downloadJob(final String target) {
        LOG.debug("*** downloadJob ***");
        ResponseStatus responseStatus = downloadJobService.download(target);
        return responseStatus.getMessage();
    }

}
