package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class GrepCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "grep <PATTERN> <SOURCE>";
    }

    @Override
    protected String[] aliases() {
        return new String[]{"g"};
    }

    @Override
    protected String description() {
        return "Search content in a sequential dataset or member. SOURCE can contain a wild card.";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.size() != 2) {
            printHelp(ctx);
            return;
        }

        var controller = ControllerFactoryContainerHolder.container()
                .getGrepController(ctx.zosConnection, args.get(0), ctx.timeout);
        String result = controller.grep(args.get(1), ctx.currDataset);
        ctx.terminal.println(result);
    }
}
