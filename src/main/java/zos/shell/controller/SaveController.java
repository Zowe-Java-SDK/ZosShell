package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.dependency.AbstractDependencyController;
import zos.shell.controller.dependency.Dependency;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.save.SaveService;

public class SaveController extends AbstractDependencyController {

    private static final Logger LOG = LoggerFactory.getLogger(SaveController.class);

    private final SaveService saveService;

    public SaveController(final SaveService saveService, final Dependency dependency) {
        super(dependency);
        LOG.debug("*** SaveController ***");
        this.saveService = saveService;
    }

    public String save(final String dataset, final String target) {
        LOG.debug("*** save ***");
        ResponseStatus responseStatus = saveService.save(dataset, target);
        return responseStatus.getMessage();
    }

}
