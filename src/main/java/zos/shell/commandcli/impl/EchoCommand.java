package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class EchoCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "echo [TEXT]";
    }

    @Override
    protected String description() {
        return "Echo text to the terminal";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.isEmpty()) {
            ctx.terminal.println("Usage: echo [TEXT]");
            return;
        }

        String text = String.join(" ", args);

        var echoController = ControllerFactoryContainerHolder.container().getEchoController();
        String result = echoController.getEcho(text);

        ctx.terminal.println(result);
    }
}
