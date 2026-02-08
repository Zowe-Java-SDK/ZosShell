package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;

public class HostnameCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(HostnameCommand.class);

    @Override
    protected String name() {
        return "Hostname";
    }

    @Override
    protected String description() {
        return "Display the hostname setting";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** HostnameCommand.run ***");
        if (!cmd.getArgList().isEmpty()) {
            printHelp(ctx);
            return;
        }

        ctx.terminal.println(ctx.zosConnection.getHost());
    }

}
