package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.constants.Constants;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.record.DatasetMember;
import zos.shell.singleton.TerminalSingleton;
import zos.shell.utility.DsnUtil;

public class RemoveCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "rm <SOURCE>";
    }

    @Override
    protected String description() {
        return "Remove a sequential dataset, member, or dataset(member).\n" +
                "SOURCE can be a single name, a wildcard (*), the current dataset (.), or include a wildcard.";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.size() != 1) {
            printHelp(ctx);
            return;
        }

        String target = args.get(0);
        String prompt;

        if (!ctx.currDataset.isBlank() && ("*".equals(target) || ".".equals(target))) {
            prompt = "Are you sure you want to delete all from " + ctx.currDataset + " y/n";
        } else if (!ctx.currDataset.isBlank() && DsnUtil.isMember(target)) {
            String candidate = ctx.currDataset + "(" + target + ")";
            prompt = "Are you sure you want to delete " + candidate + " y/n";
        } else if (ctx.currDataset.isBlank() && DsnUtil.isDataset(target)) {
            prompt = "Are you sure you want to delete dataset " + target + " y/n";
        } else if (ctx.currDataset.isBlank() && DatasetMember.getDatasetAndMember(target) == null) {
            ctx.terminal.println(Constants.DATASET_NOT_SPECIFIED);
            return;
        } else {
            prompt = "Are you sure you want to delete " + target + " y/n";
        }

        ctx.terminal.printf("%s", prompt);
        String answer = TerminalSingleton.getInstance()
                .getMainTextIO().newStringInputReader().read("?");

        if (!answer.equalsIgnoreCase("y") && !answer.equalsIgnoreCase("yes")) {
            ctx.terminal.println("delete canceled");
            return;
        }

        var ctrl = ControllerFactoryContainerHolder.container()
                .getDeleteController(ctx.zosConnection, ctx.timeout);

        ctx.terminal.println(ctrl.rm(ctx.currDataset, target));
    }

}
