package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.service.search.SearchCache;

public class MvsCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "mvs \"[COMMAND_STRING]\"";
    }

    @Override
    protected String description() {
        return "Issue an MVS console command";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        // Everything after "mvs" is treated as the console command
        if (cmd.getArgList().size() != 1) {
            ctx.terminal.println("Usage: mvs \"[COMMAND_STRING]\"");
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
