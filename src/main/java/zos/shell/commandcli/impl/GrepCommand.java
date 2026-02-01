package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import zos.shell.commandcli.AbstractCommand;
import zos.shell.commandcli.CommandContext;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class GrepCommand extends AbstractCommand {

    @Override
    protected String name() {
        return "grep";
    }

    @Override
    protected String[] aliases() {
        return new String[]{"g"};
    }

    @Override
    protected String description() {
        return "Search content in a dataset";
    }

    @Override
    protected Options options() {
        return new Options();
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.size() != 2) {
            ctx.terminal.println("Usage: grep <pattern> <dataset>");
            return;
        }

        var controller = ControllerFactoryContainerHolder.container()
                .getGrepController(ctx.zosConnection, args.get(0), ctx.timeout);
        String result = controller.grep(args.get(1), ctx.currDataset);
        ctx.terminal.println(result);
    }
}
