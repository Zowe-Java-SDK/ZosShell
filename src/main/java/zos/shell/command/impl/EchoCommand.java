package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class EchoCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(EchoCommand.class);

    @Override
    protected String name() {
        return "echo <STRING>";
    }

    @Override
    protected String description() {
        return "Echo string to the terminal and translate any environment variable with $";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** EchoCommand.run ***");
        var args = cmd.getArgList();
        if (args.size() != 1) {
            printHelp(ctx);
            return;
        }

        String text = String.join(" ", args);

        var echoController = ControllerFactoryContainerHolder.container().getEchoController();
        String result = echoController.getEcho(text);

        ctx.terminal.println(result);
    }

}
