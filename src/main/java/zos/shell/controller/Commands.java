package zos.shell.controller;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.service.search.SearchCache;
import zos.shell.service.search.SearchCacheService;

public class Commands {

    private static final Logger LOG = LoggerFactory.getLogger(Commands.class);

    private final TextTerminal<?> terminal;
    private final long timeout = Constants.FUTURE_TIMEOUT_VALUE;

    public Commands(final TextTerminal<?> terminal) {
        LOG.debug("*** Commands ***");
        this.terminal = terminal;
    }

    public void search(final SearchCache output, final String text) {
        LOG.debug("*** search ***");
        final var search = new SearchCacheService(terminal);
        search.search(output, text);
    }

}
