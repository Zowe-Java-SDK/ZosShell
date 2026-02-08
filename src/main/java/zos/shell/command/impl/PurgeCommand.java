package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class PurgeCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(PurgeCommand.class);

    @Override
    protected String name() {
        return "purge [JOBNAME]";
    }

    @Override
    protected String[] aliases() {
        return new String[]{"p"};
    }

    @Override
    protected String description() {
        return "Purge a job";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** PurgeCommand ***");
        var args = cmd.getArgList();
        if (cmd.getArgList().size() != 1) {
            printHelp(ctx);
            return;
        }

        var ctrl = ControllerFactoryContainerHolder.container()
                .getPurgeController(ctx.zosConnection, ctx.timeout);

        ctx.terminal.println(ctrl.purge(args.get(0).toUpperCase()));
    }

}
