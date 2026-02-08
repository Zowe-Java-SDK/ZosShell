package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class RenameCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(RenameCommand.class);

    @Override
    protected String name() {
        return "rename <SOURCE> <DEST>";
    }

    @Override
    protected String[] aliases() {
        return new String[]{"rn"};
    }

    @Override
    protected String description() {
        return "Rename a sequential dataset or member";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** RenameCommand ***");
        var args = cmd.getArgList();
        if (args.size() != 2) {
            printHelp(ctx);
            return;
        }

        var controller = ControllerFactoryContainerHolder.container()
                .getRenameController(ctx.zosConnection, ctx.timeout);
        String result = controller.rename(ctx.currDataset, args.get(0), args.get(1));
        ctx.terminal.println(result);
    }

}
