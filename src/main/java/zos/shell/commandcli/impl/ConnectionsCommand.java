package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.constants.Constants;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class ConnectionsCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "connections";
    }

    @Override
    protected String description() {
        return "Display configured connections";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        if (!cmd.getArgList().isEmpty()) {
            ctx.terminal.println(Constants.INVALID_COMMAND);
            return;
        }

        ControllerFactoryContainerHolder.container()
                .getChangeConnController(ctx.terminal)
                .displayConnections();
    }

}
