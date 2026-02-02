package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.constants.Constants;

public class HostnameCommand extends NoOptionCommand {

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
        if (!cmd.getArgList().isEmpty()) {
            printHelp(ctx);
            return;
        }

        ctx.terminal.println(ctx.zosConnection.getHost());
    }

}
