package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

import java.util.List;

public class CopyCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(CopyCommand.class);

    @Override
    protected String name() {
        return "copy <SOURCE> <DEST>";
    }

    @Override
    protected String description() {
        return "Copy a sequential dataset or member";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** CopyCommand.run ***");
        List<String> args = cmd.getArgList();
        if (args.size() != 2) {
            printHelp(ctx);
            return;
        }

        var copyController = ControllerFactoryContainerHolder.container()
                .getCopyController(ctx.zosConnection, ctx.timeout);

        String result = copyController.copy(ctx.currDataset, args.toArray(String[]::new));
        ctx.terminal.println(result);
    }

}
