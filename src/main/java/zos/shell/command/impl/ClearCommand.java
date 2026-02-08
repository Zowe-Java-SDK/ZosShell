package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;

public class ClearCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(ClearCommand.class);

    @Override
    protected String name() {
        return "clear";
    }

    @Override
    protected String[] aliases() {
        return new String[]{"cls"};
    }

    @Override
    protected String description() {
        return "Clear the terminal screen";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** ClearCommand.run ***");
        if (!cmd.getArgList().isEmpty()) {
            printHelp(ctx);
            return;
        }

        ctx.terminal.println();
        ctx.terminal.resetToBookmark("top");
        if (ctx.searchCache != null) {
            ctx.searchCache.getOutput().setLength(0);
            ctx.searchCache = null;
            System.gc();
        }
        ctx.terminal.println();
    }

}
