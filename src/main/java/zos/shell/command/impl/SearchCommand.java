package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactories;

public class SearchCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(SearchCommand.class);

    @Override
    protected String name() {
        return "search <PATTERN>";
    }

    @Override
    protected String[] aliases() {
        return new String[]{};
    }

    @Override
    protected String description() {
        return "Search previous command output";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** SearchCommand.run ***");
        var args = cmd.getArgList();
        if (args.size() != 1) {
            printHelp(ctx);
            return;
        }

        if (ctx.searchCache == null) {
            ctx.out("No previous output to search.");
            return;
        }

        var controller = ControllerFactories.container().getSearchCacheController();
        controller.search(ctx.searchCache, args.get(0)).forEach(ctx.terminal::println);
    }

}
