package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class StopCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "stop <JOBNAME]>";
    }

    @Override
    protected String description() {
        return "Stop a running job";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (cmd.getArgList().size() != 1) {
            printHelp(ctx);
            return;
        }

        var stopController = ControllerFactoryContainerHolder.container()
                .getStopController(ctx.zosConnection, ctx.timeout);

        String result = stopController.stop(args.get(0));
        ctx.terminal.println(result);
    }
}
