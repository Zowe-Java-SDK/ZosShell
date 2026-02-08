package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.service.search.SearchCache;

public class ProcessListCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessListCommand.class);

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
        LOG.debug("*** ProcessListCommand.run ***");
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
