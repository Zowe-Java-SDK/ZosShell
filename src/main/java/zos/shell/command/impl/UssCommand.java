package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class UssCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "ussh \"<COMMAND_STRING>\"";
    }

    @Override
    protected String description() {
        return "Issue a Unix System Services (USS) shell command";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        if (cmd.getArgList().size() != 1) {
            printHelp(ctx);
            return;
        }

        String command = String.join(" ", cmd.getArgList());

        var ctrl = ControllerFactoryContainerHolder
                .container()
                .getUssController(ctx.sshConnection);

        String result = ctrl.issueUnixCommand(command);
        ctx.terminal.println(result);
    }
}
