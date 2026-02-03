package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class TouchCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "touch <NAME>";
    }

    @Override
    protected String description() {
        return "Create empty member or dataset(member)";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.isEmpty()) {
            printHelp(ctx);
            return;
        }

        if (cmd.getArgList().size() != 1) {
            printHelp(ctx);
            return;
        }

        var ctrl = ControllerFactoryContainerHolder.container()
                .getTouchController(ctx.zosConnection, ctx.timeout);

        ctx.terminal.println(ctrl.touch(ctx.currDataset, args.get(0)));
    }

}
