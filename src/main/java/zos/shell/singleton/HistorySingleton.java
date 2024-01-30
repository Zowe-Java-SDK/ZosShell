package zos.shell.singleton;

import org.beryx.textio.TextTerminal;
import zos.shell.service.history.HistoryService;

public class HistorySingleton {

    private HistoryService history;


    private static class Holder {
        private static final HistorySingleton instance = new HistorySingleton();
    }

    private HistorySingleton() {
    }

    public static HistorySingleton getInstance() {
        return HistorySingleton.Holder.instance;
    }

    public HistoryService getHistory() {
        return this.history;
    }

    public void setHistory(final TextTerminal<?> terminal) {
        this.history = new HistoryService(terminal);
    }

}
