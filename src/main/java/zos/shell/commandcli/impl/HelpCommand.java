package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import zos.shell.commandcli.AbstractCommand;
import zos.shell.commandcli.CommandContext;
import zos.shell.constants.Constants;
import zos.shell.service.help.HelpService;
import zos.shell.service.search.SearchCache;

public class HelpCommand extends AbstractCommand {

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
    protected String usage() {
        return "help [OPTION] [COMMAND_NAME]";
    }

    @Override
    protected Options options() {
        Options opts = new Options();
        opts.addOption(Option.builder("l")
                .longOpt("list")
                .desc("List out all command names")
                .build());
        return opts;
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        if (cmd.getArgList().size() != 1) {
            printHelp(ctx);
            return;
        }

        SearchCache searchCache;

        if (cmd.hasOption("l")) {
            // If -l is specified, list all command names
            searchCache = HelpService.displayCommandNames(ctx.terminal);
        } else {
            var args = cmd.getArgList();
            if (args.isEmpty()) {
                // No args: display full help
                searchCache = HelpService.display(ctx.terminal);
            } else {
                // Display specific command help
                searchCache = HelpService.displayCommand(ctx.terminal, args.get(0));
                if (searchCache.getOutput().length() == 0) {
                    // Try abbreviations if exact command not found
                    searchCache = HelpService.displayCommandAbbreviation(ctx.terminal, args.get(0));
                }
            }
        }

        if (searchCache.getOutput().length() == 0) {
            ctx.terminal.println(Constants.HELP_COMMAND_NOT_FOUND);
        } else {
            ctx.searchCache = searchCache;
        }
    }
}
