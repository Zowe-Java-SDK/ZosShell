package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.constants.Constants;
import zos.shell.singleton.HistorySingleton;

public class HistoryCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "history";
    }

    @Override
    protected String description() {
        return "Show command history";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        if (!cmd.getArgList().isEmpty()) {
            ctx.terminal.println(Constants.INVALID_COMMAND);
            return;
        }

        var args = cmd.getArgs();
        if (args.length == 0) {
            HistorySingleton.getInstance().getHistory().displayHistory();
        } else {
            HistorySingleton.getInstance().getHistory().displayHistory(args[0]);
        }
    }

}
