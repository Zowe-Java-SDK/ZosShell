package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.service.localfile.LocalFileService;

public class LocalFilesController {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFilesController.class);

    private final LocalFileService localFileService;

    public LocalFilesController(final LocalFileService localFileService) {
        LOG.debug("*** LocalFilesController ***");
        this.localFileService = localFileService;
    }

    public StringBuilder files(String dataset) {
        LOG.debug("*** files ***");
        return localFileService.listFiles(dataset);
    }

}
