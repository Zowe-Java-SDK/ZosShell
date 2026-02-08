package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.service.search.SearchCache;

public class MvsCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(MvsCommand.class);

    @Override
    protected String name() {
        return "mvs <COMMAND_STRING>";
    }

    @Override
    protected String description() {
        return "Issue an MVS console command. All arguments following the command name are" +
                " treated as the command string and passed through as-is.";
    }

    @Override
    protected boolean stopOptionParsing() {
        return true;
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** MvsCommand.run ***");
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
