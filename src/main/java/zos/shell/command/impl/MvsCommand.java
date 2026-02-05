package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.service.search.SearchCache;

public class MvsCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "mvs <COMMAND_STRING>";
    }

    @Override
    protected String description() {
        return "Issue an MVS console command";
    }

    @Override
    protected boolean stopOptionParsing() {
        return true;
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        // Everything after "mvs" is treated as the console command
        if (cmd.getArgList().isEmpty()) {
            printHelp(ctx);
            return;
        }

        String command = String.join(" ", cmd.getArgList());

        var ctrl = ControllerFactoryContainerHolder.container()
                .getConsoleController(ctx.zosConnection, ctx.timeout);

        String result = ctrl.issueConsole(command);

        ctx.terminal.println(result);
        ctx.searchCache = new SearchCache("mvs", new StringBuilder(result));
    }
}
