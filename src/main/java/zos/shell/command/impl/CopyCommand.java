package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

import java.util.List;

public class CopyCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "copy <SOURCE> <DEST>";
    }

    @Override
    protected String description() {
        return "Copy a sequential dataset or member";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        List<String> args = cmd.getArgList();
        if (args.size() != 2) {
            printHelp(ctx);
            return;
        }

        var copyController = ControllerFactoryContainerHolder.container()
                .getCopyController(ctx.zosConnection, ctx.timeout);

        String result = copyController.copy(ctx.currDataset, args.toArray(String[]::new));
        ctx.terminal.println(result);
    }
}
