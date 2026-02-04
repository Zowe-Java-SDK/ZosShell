package zos.shell.singleton;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.service.history.HistoryService;

public final class HistorySingleton {

    private static final Logger LOG = LoggerFactory.getLogger(HistorySingleton.class);

    private HistoryService history;

    private static class Holder {
        private static final HistorySingleton instance = new HistorySingleton();
    }

    private HistorySingleton() {
        LOG.debug("*** HistorySingleton ***");
    }

    public static HistorySingleton getInstance() {
        LOG.debug("*** getInstance ***");
        return HistorySingleton.Holder.instance;
    }

    public HistoryService getHistory() {
        LOG.debug("*** getHistory ***");
        return this.history;
    }

    public void setHistory(final TextTerminal<?> terminal) {
        LOG.debug("*** setHistory ***");
        this.history = new HistoryService(terminal);
    }

}
