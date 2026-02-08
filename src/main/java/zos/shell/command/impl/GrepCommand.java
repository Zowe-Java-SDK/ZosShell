package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class GrepCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(GrepCommand.class);

    @Override
    protected String name() {
        return "grep <PATTERN> <SOURCE>";
    }

    @Override
    protected String[] aliases() {
        return new String[]{"g"};
    }

    @Override
    protected String description() {
        return "Search content in a sequential dataset, member or dataset(member). SOURCE can contain a wild card for member name only.";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** GrepCommand.run ***");
        var args = cmd.getArgList();
        if (args.size() != 2) {
            printHelp(ctx);
            return;
        }

        var controller = ControllerFactoryContainerHolder.container()
                .getGrepController(ctx.zosConnection, args.get(0), ctx.timeout);
        String result = controller.grep(args.get(1), ctx.currDataset);
        ctx.terminal.println(result);
    }

}
