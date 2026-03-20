package zos.shell.command;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.service.search.SearchCache;
import zos.shell.service.terminal.TerminalOutputService;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;

@SuppressWarnings("unused")
public class CommandContext {

    private static final Logger LOG = LoggerFactory.getLogger(CommandContext.class);

    public final TextTerminal<?> terminal;
    public final TerminalOutputService terminalOutputService;
    public ZosConnection zosConnection;
    public SshConnection sshConnection;
    public long timeout;
    public String currDataset;
    public int currDatasetMax;
    public SearchCache searchCache;
    public int currZosConnectionIndex;
    public final static ListMultimap<String, String> dataSets = ArrayListMultimap.create();

    public CommandContext(TextTerminal<?> terminal,
                          TerminalOutputService terminalOutputService,
                          ZosConnection zosConnection,
                          SshConnection sshConnection,
                          long timeout,
                          String currDataset,
                          int currDatasetMax,
                          SearchCache searchCache,
                          int currZosConnectionIndex) {
        LOG.debug("*** CommandContext ***");
        this.terminal = terminal;
        this.terminalOutputService = terminalOutputService;
        this.zosConnection = zosConnection;
        this.sshConnection = sshConnection;
        this.timeout = timeout;
        this.currDataset = currDataset;
        this.currDatasetMax = currDatasetMax;
        this.searchCache = searchCache;
        this.currZosConnectionIndex = currZosConnectionIndex;
        if (!currDataset.isEmpty() && !dataSets.containsEntry(zosConnection.getHost(), currDataset)) { // TODO
            dataSets.put(zosConnection.getHost(), currDataset);
        }
    }

    public void out(final String text) {
        LOG.debug("*** out ***");
        this.terminalOutputService.println(text);
    }

    public void out() {
        LOG.debug("*** out no arg ***");
        this.terminalOutputService.println();
    }

    public void outMultiLines(final String text) {
        LOG.debug("*** outMultiLines ***");
        this.terminalOutputService.println(text);
    }

    public void store(final String text) {
        LOG.debug("*** store ***");
        this.terminalOutputService.store(text);
    }

    public void clear() {
        LOG.debug("*** clear ***");
        this.terminalOutputService.clear();
    }

}
