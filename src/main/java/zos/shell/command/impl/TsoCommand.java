package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.service.search.SearchCache;

public class TsoCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "tso <COMMAND_STRING>";
    }

    @Override
    protected String description() {
        return "Issue a TSO command. All arguments following the command name are treated as the command string and passed through as-is.";
    }

    @Override
    protected boolean stopOptionParsing() {
        return true;
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        // Everything after "tso" is treated as the tso command
        if (cmd.getArgList().isEmpty()) {
            printHelp(ctx);
            return;
        }

        String tsoCmd = String.join(" ", cmd.getArgList());

        var env = ControllerFactoryContainerHolder
                .container()
                .getEnvVariableController();

        String acct = env.getValueByEnv("ACCOUNT_NUMBER");

        if (acct == null || acct.isBlank()) {
            ctx.terminal.println("ACCOUNT_NUMBER not set. Use the SET command to configure it.");
            return;
        }

        var tsoController = ControllerFactoryContainerHolder
                .container()
                .getTsoController(ctx.zosConnection, acct, ctx.timeout);

        String result = tsoController.issueCommand(tsoCmd);

        ctx.searchCache = new SearchCache("tso", new StringBuilder(result));
        ctx.terminal.println(result);
    }
}
