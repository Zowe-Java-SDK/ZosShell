package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.constants.Constants;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class CancelCommand extends NoOptionCommand {

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
        var args = cmd.getArgList();
        if (args.size() != 1) {
            ctx.terminal.println("Usage: cancel [JOBNAME]");
            return;
        }

        var ctrl = ControllerFactoryContainerHolder.container()
                .getCancelController(ctx.zosConnection, ctx.timeout);

        ctx.terminal.println(ctrl.cancel(args.get(0)));
    }

}

