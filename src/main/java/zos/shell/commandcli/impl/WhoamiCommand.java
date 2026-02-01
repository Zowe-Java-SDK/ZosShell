package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.constants.Constants;

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
            ctx.terminal.println(Constants.INVALID_COMMAND);
            return;
        }

        ctx.terminal.println(ctx.zosConnection.getUser());
    }

}

