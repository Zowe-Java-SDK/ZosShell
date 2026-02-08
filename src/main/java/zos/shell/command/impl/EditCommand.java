package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class EditCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(EditCommand.class);

    @Override
    protected String name() {
        return "edit <SOURCE>";
    }

    @Override
    protected String[] aliases() {
        return new String[]{};
    }

    @Override
    protected String description() {
        return "Edit a sequential dataset, member or dataset(member) using the native OS editor.\n" +
                "Save changes in the editor, then run the save command in this shell.";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** EditCommand.run ***");
        var args = cmd.getArgList();
        if (args.size() != 1) {
            printHelp(ctx);
            return;
        }

        var controller = ControllerFactoryContainerHolder.container()
                .getEditController(ctx.zosConnection, ctx.timeout);
        String result = controller.edit(ctx.currDataset, args.get(0));
        ctx.terminal.println(result);
    }

}
