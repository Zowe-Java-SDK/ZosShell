package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.singleton.HistorySingleton;

public class HistoryCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(HistoryCommand.class);

    @Override
    protected String name() {
        return "history [NUMBER]";
    }

    @Override
    protected String description() {
        return "Show command history; NUMBER is optional and limits the number of commands" +
                " displayed starting from the bottom of the history";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** HistoryCommand.run ***");
        var args = cmd.getArgs();
        if (args.length > 1) {
            printHelp(ctx);
            return;
        }
        if (args.length == 0) {
            HistorySingleton.getInstance().getHistory().displayHistory();
        } else {
            HistorySingleton.getInstance().getHistory().displayHistory(args[0]);
        }
    }

}
