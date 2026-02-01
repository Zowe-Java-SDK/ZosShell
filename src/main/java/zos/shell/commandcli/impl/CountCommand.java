package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import zos.shell.commandcli.AbstractCommand;
import zos.shell.commandcli.CommandContext;
import zos.shell.constants.Constants;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class CountCommand extends AbstractCommand {

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
    protected String usage() {
        return "count [OPTION]";
    }

    protected Options options() {
        Options o = new Options();
        o.addOption("m", "member", false, "count members in pwd");
        o.addOption("d", "dataset", false, "count datasets in pwd");
        return o;
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        boolean memberOpt = cmd.hasOption("m");
        boolean datasetOpt = cmd.hasOption("d");

        if (!memberOpt && !datasetOpt) {
            ctx.terminal.println(Constants.MISSING_COUNT_PARAM);
            return;
        }

        var controller = ControllerFactoryContainerHolder.container()
                .getCountController(ctx.zosConnection, ctx.timeout);

        if (memberOpt) {
            String result = controller.count(ctx.currDataset, "members");
            ctx.terminal.println(result);
        }

        if (datasetOpt) {
            String result = controller.count(ctx.currDataset, "datasets");
            ctx.terminal.println(result);
        }
    }
}
