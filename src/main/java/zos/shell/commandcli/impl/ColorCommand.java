package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import zos.shell.commandcli.AbstractCommand;
import zos.shell.commandcli.CommandContext;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class ColorCommand extends AbstractCommand {

    @Override
    protected String name() {
        return "color";
    }

    @Override
    protected String[] aliases() {
        return new String[]{};
    }

    @Override
    protected String description() {
        return "Change terminal color settings";
    }

    @Override
    protected Options options() {
        return new Options();
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.isEmpty() || args.size() > 2) {
            ctx.terminal.println("Usage: color <fg> [bg]");
            return;
        }

        var controller = ControllerFactoryContainerHolder.container()
                .getChangeWinController(ctx.terminal);
        String result = controller.changeColorSettings(args.get(0), args.size() == 2 ? args.get(1) : null);
        ctx.terminal.println(result);
    }
}

