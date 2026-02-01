package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;

public class TimeoutCommand extends NoOptionCommand {

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
        var args = cmd.getArgs();

        if (args.length == 0) {
            ctx.terminal.println("timeout value is " + ctx.timeout + " seconds.");
            return;
        }

        if (cmd.getArgList().size() != 1) {
            ctx.terminal.println("Usage: timeout [NEW_VALUE]");
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
