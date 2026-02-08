package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.service.search.SearchCache;

public class CatCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(CatCommand.class);

    @Override
    protected String name() {
        return "cat <SOURCE>";
    }

    @Override
    protected String description() {
        return "Display the contents of a sequential dataset or member";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** CatCommand.run ***");
        var args = cmd.getArgList();
        if (args.size() != 1) {
            printHelp(ctx);
            return;
        }

        var ctrl = ControllerFactoryContainerHolder.container()
                .getConcatController(ctx.zosConnection, ctx.timeout);

        String result = ctrl.cat(ctx.currDataset, args.get(0));
        ctx.terminal.println(result);
        ctx.searchCache = new SearchCache("cat", new StringBuilder(result));
    }

}
