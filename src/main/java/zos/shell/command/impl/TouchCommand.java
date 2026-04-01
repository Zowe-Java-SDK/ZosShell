package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactories;

public class TouchCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(TouchCommand.class);

    @Override
    protected String name() {
        return "touch <NAME>";
    }

    @Override
    protected String description() {
        return "Create empty member or dataset(member)";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** TouchCommand.run ***");
        var args = cmd.getArgList();
        if (args.isEmpty()) {
            printHelp(ctx);
            return;
        }

        if (cmd.getArgList().size() != 1) {
            printHelp(ctx);
            return;
        }

        var ctrl = ControllerFactories
                .datasetFactory()
                .getTouchController(ctx.zosConnection, ctx.timeout);

        ctx.out(ctrl.touch(ctx.currDataset, args.get(0)));
    }

}
