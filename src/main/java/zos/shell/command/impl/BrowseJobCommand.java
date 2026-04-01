package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.AbstractCommand;
import zos.shell.command.CommandContext;
import zos.shell.controller.factory.ControllerFactories;
import zos.shell.service.search.SearchCache;

public class BrowseJobCommand extends AbstractCommand {

    private static final Logger LOG = LoggerFactory.getLogger(BrowseJobCommand.class);

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
        LOG.debug("*** BrowseJobCommand.run ***");
        var args = cmd.getArgList();
        if (args.size() != 1) {
            printHelp(ctx);
            return;
        }

        boolean all = cmd.hasOption("a");
        var controller = ControllerFactories.container()
                .getBrowseJobController(ctx.zosConnection, all, ctx.timeout);

        String result = controller.browseJob(args.get(0));
        ctx.out(result);
        ctx.searchCache = new SearchCache("browsejob", new StringBuilder(result));
    }

}
