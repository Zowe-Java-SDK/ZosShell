package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.dependency.DependencyController;
import zos.shell.response.ResponseStatus;
import zos.shell.service.tso.TsoService;

public class TsoController extends DependencyController {

    private static final Logger LOG = LoggerFactory.getLogger(TsoController.class);

    private final TsoService tsoService;

    public TsoController(final TsoService tsoService, final Dependency dependency) {
        super(dependency);
        LOG.debug("*** TsoController ***");
        this.tsoService = tsoService;
    }

    public String issueCommand(final String command) {
        LOG.debug("*** issueCommand ***");
        ResponseStatus responseStatus = tsoService.issueCommand(command);
        return responseStatus.getMessage();
    }

}
