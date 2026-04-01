package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.dependency.AbstractDependencyController;
import zos.shell.controller.dependency.Dependency;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.edit.EditService;

public class EditController extends AbstractDependencyController {

    private static final Logger LOG = LoggerFactory.getLogger(EditController.class);

    private final EditService editService;

    public EditController(final EditService editService, final Dependency dependency) {
        super(dependency);
        LOG.debug("*** EditController ***");
        this.editService = editService;
    }

    public String edit(final String dataset, final String target) {
        LOG.debug("*** edit ***");
        ResponseStatus responseStatus = editService.open(dataset, target);
        return responseStatus.getMessage();
    }

}
