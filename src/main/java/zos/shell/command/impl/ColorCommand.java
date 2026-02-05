package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.utility.ColorUtil;

public class ColorCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "color <FOREGROUND_COLOR_NAME> [BACKGROUND_COLOR_NAME]";
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
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        if (args.isEmpty() || args.size() > 2) {
            printHelp(ctx);
            return;
        }

        boolean isColor = ColorUtil.validate(args.get(0));
        if (!isColor) {
            ctx.terminal.println("Invalid color: " + args.get(0));
            return;
        }
        if (args.size() > 1) {
            isColor = ColorUtil.validate(args.get(1));
            if (!isColor) {
                ctx.terminal.println("Invalid color: " + args.get(1));
                return;
            }
        }

        var controller = ControllerFactoryContainerHolder
                .container()
                .getChangeWinController(ctx.terminal);
        String result = controller.changeColorSettings(args.get(0), args.size() == 2 ? args.get(1) : null);
        ctx.terminal.println(result);
    }
}
