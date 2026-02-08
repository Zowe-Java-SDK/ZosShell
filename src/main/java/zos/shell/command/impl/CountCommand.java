package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.AbstractCommand;
import zos.shell.command.CommandContext;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class CountCommand extends AbstractCommand {

    private static final Logger LOG = LoggerFactory.getLogger(CountCommand.class);

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
        return "count <OPTION>";
    }

    protected Options options() {
        Options o = new Options();
        o.addOption("m", "member", false, "count members in pwd");
        o.addOption("d", "dataset", false, "count datasets in pwd");
        return o;
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** CountCommand.run ***");
        boolean memberOpt = cmd.hasOption("m");
        boolean datasetOpt = cmd.hasOption("d");

        if (!memberOpt && !datasetOpt) {
            printHelp(ctx);
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
