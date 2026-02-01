package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.service.search.SearchCache;

public class CatCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "cat [SEQUENTIAL_DATASET_NAME] [MEMBER]";
    }

    @Override
    protected String description() {
        return "Catenate a file";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.isEmpty()) {
            ctx.terminal.println("cat [SEQUENTIAL_DATASET_NAME] [MEMBER]");
            return;
        }

        var ctrl = ControllerFactoryContainerHolder.container()
                .getConcatController(ctx.zosConnection, ctx.timeout);

        String result = ctrl.cat(ctx.currDataset, args.get(0));
        ctx.terminal.println(result);
        ctx.searchCache = new SearchCache("cat", new StringBuilder(result));
    }

}

