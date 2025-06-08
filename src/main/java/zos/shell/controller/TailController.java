package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.controller.dependency.Dependency;
import zos.shell.controller.dependency.DependencyController;
import zos.shell.response.ResponseStatus;
import zos.shell.service.job.tail.TailService;
import zos.shell.service.search.SearchCache;

import java.util.Arrays;

public class TailController extends DependencyController {

    private static final Logger LOG = LoggerFactory.getLogger(TailController.class);

    private final TailService tailService;

    public TailController(final TailService tailService, final Dependency dependency) {
        super(dependency);
        LOG.debug("*** TailController ***");
        this.tailService = tailService;
    }

    public SearchCache tail(final String[] params) {
        LOG.debug("*** tail ***");
        long allCount = Arrays.stream(params).filter("ALL"::equalsIgnoreCase).count();
        ResponseStatus responseStatus = tailService.tail(params, allCount == 1);
        return new SearchCache("tail", new StringBuilder(responseStatus.getMessage()));
    }

}
