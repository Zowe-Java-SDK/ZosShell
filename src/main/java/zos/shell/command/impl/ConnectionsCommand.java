package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class ConnectionsCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionsCommand.class);

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
        LOG.debug("*** ConnectionsCommand.run ***");
        if (!cmd.getArgList().isEmpty()) {
            printHelp(ctx);
            return;
        }

        ControllerFactoryContainerHolder.container()
                .getChangeConnController(ctx.terminal)
                .displayConnections();
    }

}
