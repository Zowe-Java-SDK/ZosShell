package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.response.ResponseStatus;

public class CdCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "cd <SOURCE>";
    }

    @Override
    protected String description() {
        return "Change directory to a PDS or MLQ";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.isEmpty()) {
            ctx.terminal.println("Usage: cd <SOURCE>");
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
