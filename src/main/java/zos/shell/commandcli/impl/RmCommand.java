package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.singleton.TerminalSingleton;

public class RmCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "rm <SOURCE>";
    }

    @Override
    protected String description() {
        return "Remove sequential dataset(s) or member(s)";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.size() != 1) {
            printHelp(ctx);
            return;
        }

        String target = args.get(0);

        ctx.terminal.printf("Are you sure you want to delete %s y/n", target);
        String answer = TerminalSingleton.getInstance()
                .getMainTextIO().newStringInputReader().read("?");

        if (!answer.equalsIgnoreCase("y") && !answer.equalsIgnoreCase("yes")) {
            ctx.terminal.println("delete canceled");
            return;
        }

        var ctrl = ControllerFactoryContainerHolder.container()
                .getDeleteController(ctx.zosConnection, ctx.timeout);

        ctx.terminal.println(ctrl.rm(ctx.currDataset, target));
    }

}
