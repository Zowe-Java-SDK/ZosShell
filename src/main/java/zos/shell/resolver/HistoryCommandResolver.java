package zos.shell.resolver;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.service.history.HistoryService;

public class HistoryCommandResolver {

    private static final Logger LOG = LoggerFactory.getLogger(HistoryCommandResolver.class);

    private final TextTerminal<?> terminal;
    private final HistoryService historyService;

    public HistoryCommandResolver(TextTerminal<?> terminal, HistoryService historyService) {
        LOG.debug("*** HistoryCommandResolver ***");
        this.terminal = terminal;
        this.historyService = historyService;
    }

    /**
     * Resolve a command starting with "!" from history.
     * Returns null if the command cannot be resolved.
     */
    public String[] resolve(String[] command) {
        LOG.debug("*** resolve ***");
        if (command.length == 0 || !command[0].startsWith("!")) {
            return command;
        }

        String cmd = String.join(" ", command);
        if (cmd.length() == 1) { // just "!"
            terminal.println(Constants.MISSING_PARAMETERS);
            return null;
        }

        String subCmd = cmd.substring(1); // remove "!"
        String resolved;

        if ("!".equals(subCmd)) {
            resolved = historyService.getLastHistory();
        } else if (isNumeric(subCmd)) {
            resolved = historyService.getHistoryByIndex(Integer.parseInt(subCmd) - 1);
        } else {
            resolved = historyService.getLastHistoryByValue(subCmd);
        }

        if (resolved == null) {
            terminal.println("History not found for: " + cmd);
            return null;
        }

        return resolved.split("\\s+");
    }

    private boolean isNumeric(String str) {
        LOG.debug("*** isNumeric ***");
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
