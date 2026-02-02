package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class PurgeCommand extends NoOptionCommand {

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
