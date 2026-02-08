package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;

public class TimeoutCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(TimeoutCommand.class);

    @Override
    protected String name() {
        return "timeout [NEW_VALUE]";
    }

    @Override
    protected String[] aliases() {
        return new String[]{"t"};
    }

    @Override
    protected String description() {
        return "Display or set timeout value";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** TimeoutCommand.run ***");
        var args = cmd.getArgs();

        if (args.length == 0) {
            ctx.terminal.println("timeout value is " + ctx.timeout + " seconds.");
            return;
        }

        if (cmd.getArgList().size() != 1) {
            printHelp(ctx);
            return;
        }

        try {
            ctx.timeout = Long.parseLong(args[0]);
            ctx.terminal.println("timeout value set to " + ctx.timeout + " seconds.");
        } catch (NumberFormatException e) {
            ctx.terminal.println("Invalid timeout value");
        }
    }

}
