package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class SubmitCommand extends NoOptionCommand {

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
