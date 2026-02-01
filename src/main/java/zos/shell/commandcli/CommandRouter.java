package zos.shell.commandcli;

import org.beryx.textio.TextTerminal;
import zos.shell.constants.Constants;
import zos.shell.service.search.SearchCache;
import zos.shell.singleton.ConnSingleton;
import zos.shell.singleton.HistorySingleton;

public class CommandRouter {

    private final TextTerminal<?> terminal;
    private final CommandRegistry registry = new CommandRegistry();

    private long timeout = Constants.FUTURE_TIMEOUT_VALUE;
    private String currDataset = "";
    private int currDatasetMax = 0;
    private SearchCache searchCache;
    private int currZosConnectionIndex;

    public CommandRouter(TextTerminal<?> terminal) {
        this.terminal = terminal;
    }

    public void routeCommand(String input) {
        String cmdName = input.split("\\s+")[0].toLowerCase();
        CommandHandler handler = registry.get(cmdName);

        if (handler == null) {
            terminal.println(Constants.INVALID_COMMAND);
            return;
        }

        HistorySingleton.getInstance().getHistory().addHistory(input.split("\\s+"));

        CommandContext ctx = new CommandContext(
                terminal,
                ConnSingleton.getInstance().getCurrZosConnection(),
                ConnSingleton.getInstance().getCurrSshConnection(),
                timeout,
                currDataset,
                currDatasetMax,
                searchCache,
                currZosConnectionIndex
        );

        handler.execute(ctx, input);

        // propagate the mutable state back
        this.timeout = ctx.timeout;
        this.currDataset = ctx.currDataset;
        this.currDatasetMax = ctx.currDatasetMax;
        this.searchCache = ctx.searchCache;
        this.currZosConnectionIndex = ctx.currZosConnectionIndex;
    }

}

