package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import zos.shell.commandcli.AbstractCommand;
import zos.shell.commandcli.CommandContext;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

import java.util.List;

public class CopyCommand extends AbstractCommand {

    @Override
    protected String name() {
        return "copy [SOURCE] [DEST]";
    }

    @Override
    protected String description() {
        return "Copy a dataset or member";
    }

    @Override
    protected Options options() {
        return new Options(); // no special options for now
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
