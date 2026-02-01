package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.constants.Constants;
import zos.shell.service.help.HelpService;
import zos.shell.service.search.SearchCache;

public class HelpCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "help";
    }

    @Override
    protected String[] aliases() {
        return new String[]{"h"};
    }

    @Override
    protected String description() {
        return "Display help information";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        SearchCache searchCache;
        if (args.isEmpty()) {
            searchCache = HelpService.display(ctx.terminal);
        } else if ("-l".equalsIgnoreCase(args.get(0))) {
            searchCache = HelpService.displayCommandNames(ctx.terminal);
        } else {
            searchCache = HelpService.displayCommand(ctx.terminal, args.get(0));
            if (searchCache.getOutput().length() == 0) {
                searchCache = HelpService.displayCommandAbbreviation(ctx.terminal, args.get(0));
            }
        }

        if (searchCache.getOutput().length() == 0) {
            ctx.terminal.println(Constants.HELP_COMMAND_NOT_FOUND);
        } else {
            ctx.searchCache = searchCache;
        }
    }
}
