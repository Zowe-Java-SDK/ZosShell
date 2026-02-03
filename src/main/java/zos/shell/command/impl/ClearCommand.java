package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;

public class ClearCommand extends NoOptionCommand {

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
