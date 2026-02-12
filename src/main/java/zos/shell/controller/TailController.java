package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.dependency.DependencyController;
import zos.shell.response.ResponseStatus;
import zos.shell.service.job.tail.TailService;

public class TailController extends DependencyController {

    private static final Logger LOG = LoggerFactory.getLogger(TailController.class);

    private final TailService tailService;

    public TailController(final TailService tailService, final Dependency dependency) {
        super(dependency);
        LOG.debug("*** TailController ***");
        this.tailService = tailService;
    }

    public String tail(final String target, int lines) {
        LOG.debug("*** tail ***");
        ResponseStatus responseStatus = tailService.tail(target, lines);
        LOG.info("tail response status: {}", responseStatus);
        return responseStatus.getMessage();
    }

}
