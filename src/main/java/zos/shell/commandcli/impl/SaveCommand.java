package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class SaveCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "save <SOURCE>";
    }

    @Override
    protected String description() {
        return "Save a sequential dataset, member and dataset(member)";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.size() != 1) {
            printHelp(ctx);
            return;
        }

        var saveController = ControllerFactoryContainerHolder.container()
                .getSaveController(ctx.zosConnection, ctx.timeout);

        String result = saveController.save(ctx.currDataset, args.get(0));
        ctx.terminal.println(result);
    }
}
