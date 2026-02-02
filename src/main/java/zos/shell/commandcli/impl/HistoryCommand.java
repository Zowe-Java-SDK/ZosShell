package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.singleton.HistorySingleton;

public class HistoryCommand extends NoOptionCommand {

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
        var args = cmd.getArgs();
        if (args.length == 0) {
            HistorySingleton.getInstance().getHistory().displayHistory();
        } else {
            HistorySingleton.getInstance().getHistory().displayHistory(args[0]);
        }
    }

}
