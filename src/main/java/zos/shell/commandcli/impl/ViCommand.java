package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import zos.shell.commandcli.AbstractCommand;
import zos.shell.commandcli.CommandContext;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class ViCommand extends AbstractCommand {

    @Override
    protected String name() {
        return "vi";
    }

    @Override
    protected String[] aliases() {
        return new String[]{};
    }

    @Override
    protected String description() {
        return "Edit a dataset or member";
    }

    @Override
    protected Options options() {
        return new Options();
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.size() != 1) {
            ctx.terminal.println("Usage: vi <member>");
            return;
        }

        var controller = ControllerFactoryContainerHolder.container()
                .getEditController(ctx.zosConnection, ctx.timeout);
        String result = controller.edit(ctx.currDataset, args.get(0));
        ctx.terminal.println(result);
    }
}
