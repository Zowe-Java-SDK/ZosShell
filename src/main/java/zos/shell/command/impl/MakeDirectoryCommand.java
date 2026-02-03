package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.singleton.TerminalSingleton;

public class MakeDirectoryCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "mkdir <NAME>";
    }

    @Override
    protected String[] aliases() {
        return new String[]{};
    }

    @Override
    protected String description() {
        return "Create a new dataset or member";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.size() != 1) {
            printHelp(ctx);
            return;
        }

        TerminalSingleton.getInstance().setDisableKeys(true);
        var controller = ControllerFactoryContainerHolder.container()
                .getMakeDirController(ctx.zosConnection, ctx.terminal, ctx.timeout);
        controller.mkdir(TerminalSingleton.getInstance().getMainTextIO(), ctx.currDataset, args.get(0));
        TerminalSingleton.getInstance().setDisableKeys(false);
    }
}
