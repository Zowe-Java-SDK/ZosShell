package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.constants.Constants;

public class PwdCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(PwdCommand.class);

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
        LOG.debug("*** PwdCommand ***");
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
