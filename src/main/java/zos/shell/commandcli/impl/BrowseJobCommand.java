package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import zos.shell.commandcli.AbstractCommand;
import zos.shell.commandcli.CommandContext;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.service.search.SearchCache;

public class BrowseJobCommand extends AbstractCommand {

    @Override
    protected String name() {
        return "browsejob";
    }

    @Override
    protected String[] aliases() {
        return new String[]{"bj"};
    }

    @Override
    protected String description() {
        return "Browse JES job output";
    }

    @Override
    protected String usage() {
        return "browsejob [OPTION] <JOBNAME>";
    }

    @Override
    protected Options options() {
        Options opts = new Options();
        opts.addOption(Option.builder("a")
                .longOpt("all")
                .desc("Display all job steps")
                .build());
        return opts;
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.size() != 1) {
            ctx.terminal.println("Usage: browsejob [OPTION] <JOBNAME>");
            return;
        }

        boolean all = cmd.hasOption("a");
        var controller = ControllerFactoryContainerHolder.container()
                .getBrowseJobController(ctx.zosConnection, all, ctx.timeout);

        String result = controller.browseJob(args.get(0));
        ctx.terminal.println(result);
        ctx.searchCache = new SearchCache("browsejob", new StringBuilder(result));
    }

}
