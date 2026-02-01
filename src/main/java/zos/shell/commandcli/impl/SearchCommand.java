package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import zos.shell.commandcli.AbstractCommand;
import zos.shell.commandcli.CommandContext;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class SearchCommand extends AbstractCommand {

    @Override
    protected String name() {
        return "search";
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
    protected Options options() {
        return new Options();
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.size() != 1) {
            ctx.terminal.println("Usage: search <pattern>");
            return;
        }

        if (ctx.searchCache == null) {
            ctx.terminal.println("No previous output to search.");
            return;
        }

        var controller = ControllerFactoryContainerHolder.container().getSearchCacheController();
        controller.search(ctx.searchCache, args.get(0)).forEach(ctx.terminal::println);
    }
}
