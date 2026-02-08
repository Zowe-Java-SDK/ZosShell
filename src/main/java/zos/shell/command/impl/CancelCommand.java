package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class CancelCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(CancelCommand.class);

    @Override
    protected String name() {
        return "cancel <JOBNAME>";
    }

    @Override
    protected String description() {
        return "Cancel a job";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** CancelCommand.run ***");
        var args = cmd.getArgList();
        if (args.size() != 1) {
            printHelp(ctx);
            return;
        }

        var ctrl = ControllerFactoryContainerHolder.container()
                .getCancelController(ctx.zosConnection, ctx.timeout);

        ctx.terminal.println(ctrl.cancel(args.get(0)));
    }

}
