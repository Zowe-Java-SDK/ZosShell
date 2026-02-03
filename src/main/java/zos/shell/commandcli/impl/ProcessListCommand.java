package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.service.search.SearchCache;

public class ProcessListCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "ps [JOBNAME]";
    }

    @Override
    protected String description() {
        return "List jobs on system";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        if (cmd.getArgList().size() > 1) {
            printHelp(ctx);
            return;
        }

        var ctrl = ControllerFactoryContainerHolder.container()
                .getProcessLstController(ctx.zosConnection, ctx.timeout);

        var args = cmd.getArgList();
        String result = args.isEmpty()
                ? ctrl.processList()
                : ctrl.processList(args.get(0));

        ctx.terminal.println(result);
        ctx.searchCache = new SearchCache("ps", new StringBuilder(result));
    }

}
