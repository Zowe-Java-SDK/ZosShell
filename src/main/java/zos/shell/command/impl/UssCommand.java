package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class UssCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "uss <COMMAND_STRING>";
    }

    @Override
    protected String description() {
        return "Issue a Unix System Services (USS) shell command. All arguments following the command name are treated as the command string and passed through as-is.";
    }

    @Override
    protected boolean stopOptionParsing() {
        return true;
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        // Everything after "uss" is treated as the uss command
        if (cmd.getArgList().isEmpty()) {
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
