package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.constants.Constants;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class CountCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "count";
    }

    @Override
    protected String[] aliases() {
        return new String[]{};
    }

    @Override
    protected String description() {
        return "Count members or datasets";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.isEmpty() || !(args.get(0).equalsIgnoreCase("members") || args.get(0).equalsIgnoreCase("datasets"))) {
            ctx.terminal.println(Constants.MISSING_COUNT_PARAM);
            return;
        }

        var controller = ControllerFactoryContainerHolder.container()
                .getCountController(ctx.zosConnection, ctx.timeout);
        String result = controller.count(ctx.currDataset, args.get(0));
        ctx.terminal.println(result);
    }
}
