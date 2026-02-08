package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.response.ResponseStatus;

public class ChangeDirectoryCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeDirectoryCommand.class);

    @Override
    protected String name() {
        return "cd <SOURCE>";
    }

    @Override
    protected String description() {
        return "Change directory to a PDS or .MLQ";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** ChangeDirectoryCommand.run ***");
        var args = cmd.getArgList();
        if (args.size() != 1) {
            printHelp(ctx);
            return;
        }

        var ctrl = ControllerFactoryContainerHolder.container().getChangeDirController();
        ResponseStatus rs = ctrl.cd(ctx.currDataset, args.get(0).toUpperCase());

        if (!rs.isStatus()) {
            ctx.terminal.println(rs.getMessage());
            return;
        }

        ctx.currDataset = rs.getOptionalData();
        ctx.currDatasetMax = Math.max(ctx.currDatasetMax, ctx.currDataset.length());
        ctx.terminal.println("set to " + ctx.currDataset);
    }

}
