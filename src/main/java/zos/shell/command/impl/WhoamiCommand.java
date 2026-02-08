package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;

public class WhoamiCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(WhoamiCommand.class);

    @Override
    protected String name() {
        return "whoami";
    }

    @Override
    protected String description() {
        return "Display current user";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** WhoamiCommand.run ***");
        if (!cmd.getArgList().isEmpty()) {
            printHelp(ctx);
            return;
        }

        ctx.terminal.println(ctx.zosConnection.getUser());
    }

}
