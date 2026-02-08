package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.AbstractCommand;
import zos.shell.command.CommandContext;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.service.search.SearchCache;

public class TailCommand extends AbstractCommand {

    private static final Logger LOG = LoggerFactory.getLogger(TailCommand.class);

    @Override
    protected String name() {
        return "tail <JOBNAME>";
    }

    @Override
    protected String description() {
        return "Display the contents of a job from the bottom";
    }

    @Override
    protected Options options() {
        Options opts = new Options();
        opts.addOption(Option.builder("n")
                .longOpt("lines")
                .hasArg()
                .argName("count")
                .desc("Number of lines to display")
                .build());
        return opts;
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** TailCommand.run ***");
        var args = cmd.getArgs();
        if (cmd.getArgList().size() != 1) {
            printHelp(ctx);
            return;
        }

        int lines = 10;
        if (cmd.hasOption("n")) {
            try {
                lines = Integer.parseInt(cmd.getOptionValue("n"));
            } catch (NumberFormatException e) {
                ctx.terminal.println("Invalid line count");
                return;
            }
        }

        var controller = ControllerFactoryContainerHolder.container()
                .getTailController(ctx.zosConnection, ctx.terminal, lines);

        String result = String.valueOf(controller.tail(args));
        ctx.terminal.println(result);
        ctx.searchCache = new SearchCache("tail", new StringBuilder(result));
    }

}
