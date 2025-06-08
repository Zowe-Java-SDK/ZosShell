package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.dependency.DependencyController;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.count.CountService;

public class CountController extends DependencyController {

    private static final Logger LOG = LoggerFactory.getLogger(CountController.class);

    private final CountService countService;

    public CountController(final CountService countService, final Dependency dependency) {
        super(dependency);
        LOG.debug("*** CountController ***");
        this.countService = countService;
    }

    public String count(final String dataset, final String filter) {
        LOG.debug("*** count ***");
        ResponseStatus responseStatus = countService.count(dataset, filter);
        return responseStatus.getMessage();
    }

}
