package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import zos.shell.commandcli.AbstractCommand;
import zos.shell.commandcli.CommandContext;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class UssCommand extends AbstractCommand {

    @Override
    protected String name() {
        return "ussh \"[COMMAND_STRING]\"";
    }

    @Override
    protected String description() {
        return "Issue a Unix System Services (USS) shell command";
    }

    @Override
    protected Options options() {
        // No flags; free-form USS command
        return new Options();
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        if (cmd.getArgList().isEmpty()) {
            ctx.terminal.println("Missing USS command");
            return;
        }

        if (cmd.getArgList().size() != 1) {
            ctx.terminal.println("Usage: ussh \"[COMMAND_STRING]\"");
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

