package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.dependency.DependencyController;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.delete.DeleteService;

public class DeleteController extends DependencyController {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteController.class);

    private final DeleteService deleteService;

    public DeleteController(final DeleteService deleteService, final Dependency dependency) {
        super(dependency);
        LOG.debug("*** DeleteController ***");
        this.deleteService = deleteService;
    }

    public String rm(final String dataset, final String param) {
        LOG.debug("*** rm ***");
        ResponseStatus responseStatus = deleteService.delete(dataset, param);
        return responseStatus.getMessage();
    }

}
