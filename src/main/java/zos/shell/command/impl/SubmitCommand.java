package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class SubmitCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(SubmitCommand.class);

    @Override
    protected String name() {
        return "submit <JOBNAME>";
    }

    @Override
    protected String description() {
        return "Submit a job to the system";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** SubmitCommand.run ***");
        var args = cmd.getArgList();
        if (cmd.getArgList().size() != 1) {
            printHelp(ctx);
            return;
        }

        var ctrl = ControllerFactoryContainerHolder.container()
                .getSubmitController(ctx.zosConnection, ctx.timeout);

        ctx.terminal.println(ctrl.submit(ctx.currDataset, args.get(0)));
    }

}
