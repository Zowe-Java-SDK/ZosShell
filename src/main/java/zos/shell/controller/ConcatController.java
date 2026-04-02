package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.dependency.AbstractDependencyController;
import zos.shell.controller.dependency.Dependency;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.concat.ConcatService;

public class ConcatController extends AbstractDependencyController {

    private static final Logger LOG = LoggerFactory.getLogger(ConcatController.class);

    private final ConcatService concatService;

    public ConcatController(final ConcatService concatService, final Dependency dependency) {
        super(dependency);
        LOG.debug("*** ConCatController ***");
        this.concatService = concatService;
    }

    public String cat(final String dataset, final String target) {
        LOG.debug("*** cat ***");
        ResponseStatus responseStatus = concatService.cat(dataset, target);
        return responseStatus.getMessage();
    }

}
