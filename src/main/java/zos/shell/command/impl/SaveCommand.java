package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class SaveCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(SaveCommand.class);

    @Override
    protected String name() {
        return "save <SOURCE>";
    }

    @Override
    protected String description() {
        return "Save a sequential dataset, member and dataset(member)";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** SaveCommand.run ***");
        var args = cmd.getArgList();
        if (args.size() != 1) {
            printHelp(ctx);
            return;
        }

        var saveController = ControllerFactoryContainerHolder.container()
                .getSaveController(ctx.zosConnection, ctx.timeout);

        String result = saveController.save(ctx.currDataset, args.get(0));
        ctx.terminal.println(result);
    }

}
