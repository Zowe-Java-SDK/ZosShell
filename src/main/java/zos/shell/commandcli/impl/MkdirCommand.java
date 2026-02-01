package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.singleton.TerminalSingleton;

public class MkdirCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "mkdir";
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
            ctx.terminal.println("Usage: mkdir <name>");
            return;
        }

        TerminalSingleton.getInstance().setDisableKeys(true);
        var controller = ControllerFactoryContainerHolder.container()
                .getMakeDirController(ctx.zosConnection, ctx.terminal, ctx.timeout);
        controller.mkdir(TerminalSingleton.getInstance().getMainTextIO(), ctx.currDataset, args.get(0));
        TerminalSingleton.getInstance().setDisableKeys(false);
    }
}
