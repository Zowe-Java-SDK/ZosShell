package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class StopCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "stop [JOB_NAME]";
    }

    @Override
    protected String description() {
        return "Stop a running job";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.isEmpty()) {
            ctx.terminal.println("Usage: stop [JOB_NAME]");
            return;
        }

        var stopController = ControllerFactoryContainerHolder.container()
                .getStopController(ctx.zosConnection, ctx.timeout);

        String result = stopController.stop(args.get(0));
        ctx.terminal.println(result);
    }
}
