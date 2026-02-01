package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import zos.shell.commandcli.AbstractCommand;
import zos.shell.commandcli.CommandContext;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class UsermodCommand extends AbstractCommand {

    @Override
    protected String name() {
        return "usermod";
    }

    @Override
    protected String[] aliases() {
        return new String[]{};
    }

    @Override
    protected String description() {
        return "Modify the current user settings";
    }

    @Override
    protected Options options() {
        return new Options();
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.size() != 1) {
            ctx.terminal.println("Usage: usermod <flag>");
            return;
        }

        var controller = ControllerFactoryContainerHolder.container()
                .getUsermodController(ctx.zosConnection, ctx.currZosConnectionIndex);
        String result = controller.change(args.get(0));
        ctx.terminal.println(result);
    }
}
