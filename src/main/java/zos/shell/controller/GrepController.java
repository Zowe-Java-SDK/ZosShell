package zos.shell.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.service.grep.GrepService;

public class GrepController {

    private static final Logger LOG = LoggerFactory.getLogger(GrepController.class);

    private final GrepService grepService;

    public GrepController(final GrepService grepService) {
        LOG.debug("*** GrepController ***");
        this.grepService = grepService;
    }

    public String grep(final String target, final String dataset) {
        LOG.debug("*** grep ***");
        var results = new StringBuilder();
        grepService.search(dataset, target).forEach(i -> {
            if (i.endsWith("\n")) {
                results.append(i);
            } else {
                results.append(i).append("\n");
            }
        });
        return results.toString();
    }

}
