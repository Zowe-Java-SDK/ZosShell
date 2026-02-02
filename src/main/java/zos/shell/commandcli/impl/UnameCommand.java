package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.constants.Constants;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class UnameCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "uname";
    }

    @Override
    protected String description() {
        return "Display z/OS system information";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        if (!cmd.getArgList().isEmpty()) {
            ctx.terminal.println(Constants.INVALID_COMMAND);
            return;
        }

        var ctrl = ControllerFactoryContainerHolder.container()
                .getUnameController(ctx.zosConnection, ctx.timeout);
        ctx.terminal.println(ctrl.uname(ctx.zosConnection));
    }

}
