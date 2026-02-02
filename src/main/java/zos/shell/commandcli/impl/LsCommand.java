package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import zos.shell.commandcli.AbstractCommand;
import zos.shell.commandcli.CommandContext;
import zos.shell.constants.Constants;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.utility.DsnUtil;

public class LsCommand extends AbstractCommand {

    @Override
    protected String name() {
        return "ls";
    }

    @Override
    protected String description() {
        return "List dataset(s) or member(s)";
    }

    @Override
    protected String usage() {
        return "ls [OPTION] [SOURCE]";
    }

    @Override
    protected Options options() {
        Options o = new Options();
        o.addOption("l", "long", false, "long listing with attributes");
        o.addOption(null, "long-no-attr", false, "long listing without attribute info");
        return o;
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        if (cmd.getArgList().size() > 2) {
            printHelp(ctx);
            return;
        }

        boolean longList = cmd.hasOption("l") || cmd.hasOption("long-no-attr");
        boolean withAttr = cmd.hasOption("l");

        var listingController = ControllerFactoryContainerHolder
                .container()
                .getListingController(ctx.zosConnection, ctx.terminal, ctx.timeout);

        var args = cmd.getArgList();

        // no source specified
        if (args.isEmpty()) {
            if (ctx.currDataset.isBlank()) {
                ctx.terminal.println(Constants.DATASET_NOT_SPECIFIED);
                return;
            }
            if (longList) {
                listingController.lsl(ctx.currDataset, withAttr);
            } else {
                listingController.ls(ctx.currDataset);
            }
            return;
        }

        // at this point, source specified check if
        // it is either partition dataset or member
        String target = args.get(0);

        // list dataset
        if (DsnUtil.isDataset(target)) {
            if (longList)
                listingController.lsl(null, target, withAttr);
            else
                listingController.ls(target);
            return;
        }

        // error out if no pwd set
        if (ctx.currDataset.isBlank()) {
            ctx.terminal.println(Constants.DATASET_NOT_SPECIFIED);
            return;
        }

        // list member
        if (longList) {
            listingController.lsl(target, ctx.currDataset, withAttr);
        } else {
            listingController.ls(target, ctx.currDataset);
        }
    }
}
