package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.dependency.DependencyController;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.copy.CopyService;

public class CopyController extends DependencyController {

    private static final Logger LOG = LoggerFactory.getLogger(CopyController.class);

    private final CopyService copyService;

    public CopyController(final CopyService copyService, final Dependency dependency) {
        super(dependency);
        LOG.debug("*** CopyController ***");
        this.copyService = copyService;
    }

    public String copy(final String dataset, final String[] params) {
        LOG.debug("*** copy ***");
        ResponseStatus responseStatus = copyService.copy(dataset, params);
        return responseStatus.getMessage();
    }

}
