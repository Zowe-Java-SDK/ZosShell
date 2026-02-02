package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.utility.ColorUtil;

public class ColorCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "color <FOURGOUND_COLOR_NAME> [BACKGROUND_COLOR_NAME]";
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
            ctx.terminal.println("Usage: color <FOREGROUND_COLOR_NAME> [BACKGROUND_COLOR_NAME]");
            return;
        }

        try {
            ColorUtil.validate(args.get(0));
            ColorUtil.validate(args.size() == 2 ? args.get(1) : null);
        } catch (IllegalArgumentException e) {
            ctx.terminal.println(e.getMessage());
            return;
        }

        var controller = ControllerFactoryContainerHolder
                .container()
                .getChangeWinController(ctx.terminal);
        String result = controller.changeColorSettings(args.get(0), args.size() == 2 ? args.get(1) : null);
        ctx.terminal.println(result);
    }
}
