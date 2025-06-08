package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.dependency.DependencyController;
import zos.shell.response.ResponseStatus;
import zos.shell.service.rename.RenameService;

public class RenameController extends DependencyController {

    private static final Logger LOG = LoggerFactory.getLogger(RenameController.class);

    private final RenameService renameService;

    public RenameController(final RenameService renameService, final Dependency dependency) {
        super(dependency);
        LOG.debug("*** RenameController ***");
        this.renameService = renameService;
    }

    public String rename(final String dataset, final String target, final String source) {
        LOG.debug("*** rename ***");
        ResponseStatus responseStatus = renameService.rename(dataset, target, source);
        return responseStatus.getMessage();
    }

}
