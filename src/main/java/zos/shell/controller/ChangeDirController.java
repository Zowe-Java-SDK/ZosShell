package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.service.change.ChangeDirectoryService;

public class ChangeDirController {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeDirController.class);

    private final ChangeDirectoryService changeDirService;

    public ChangeDirController(final ChangeDirectoryService changeDirService) {
        LOG.debug("*** ChangeDirController ***");
        this.changeDirService = changeDirService;
    }

    public ResponseStatus cd(final String dataset, final String target) {
        LOG.debug("*** cd ***");
        return changeDirService.cd(dataset, target);
    }

}
