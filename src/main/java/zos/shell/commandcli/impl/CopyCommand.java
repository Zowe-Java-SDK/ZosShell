package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

import java.util.List;

public class CopyCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "copy [SOURCE] [DEST]";
    }

    @Override
    protected String description() {
        return "Copy a dataset or member";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        List<String> args = cmd.getArgList();
        if (args.size() != 2) {
            ctx.terminal.println("Usage: copy [SOURCE] [DEST]");
            return;
        }

        var copyController = ControllerFactoryContainerHolder.container()
                .getCopyController(ctx.zosConnection, ctx.timeout);

        String result = copyController.copy(ctx.currDataset, args.toArray(String[]::new));
        ctx.terminal.println(result);
    }
}
