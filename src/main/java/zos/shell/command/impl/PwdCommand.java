package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.constants.Constants;

public class PwdCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "pwd";
    }

    @Override
    protected String description() {
        return "Display current dataset";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        if (!cmd.getArgList().isEmpty()) {
            printHelp(ctx);
            return;
        }

        if (ctx.currDataset.isBlank()) {
            ctx.terminal.println(Constants.DATASET_NOT_SPECIFIED);
            return;
        }
        ctx.terminal.println(ctx.currDataset);
    }

}
