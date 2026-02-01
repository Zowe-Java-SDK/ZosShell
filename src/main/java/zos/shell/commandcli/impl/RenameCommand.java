package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class RenameCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "rename";
    }

    @Override
    protected String[] aliases() {
        return new String[]{"rn"};
    }

    @Override
    protected String description() {
        return "Rename a dataset or member";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.size() != 2) {
            ctx.terminal.println("Usage: rename <old_name> <new_name>");
            return;
        }

        var controller = ControllerFactoryContainerHolder.container()
                .getRenameController(ctx.zosConnection, ctx.timeout);
        String result = controller.rename(ctx.currDataset, args.get(0), args.get(1));
        ctx.terminal.println(result);
    }
}
