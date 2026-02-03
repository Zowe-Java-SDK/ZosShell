package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;

public class WhoamiCommand extends NoOptionCommand {

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
        if (!cmd.getArgList().isEmpty()) {
            printHelp(ctx);
            return;
        }

        ctx.terminal.println(ctx.zosConnection.getUser());
    }

}
