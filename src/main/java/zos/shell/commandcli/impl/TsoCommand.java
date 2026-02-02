package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.service.search.SearchCache;

public class TsoCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "tso \"<COMMAND_STRING>\"";
    }

    @Override
    protected String description() {
        return "Issue a TSO command";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        if (cmd.getArgList().size() != 1) {
            printHelp(ctx);
            return;
        }

        String tsoCmd = String.join(" ", cmd.getArgList());

        var env = ControllerFactoryContainerHolder
                .container()
                .getEnvVariableController();

        String acct = env.getValueByEnv("ACCOUNT_NUMBER");

        if (acct == null || acct.isBlank()) {
            ctx.terminal.println("ACCOUNT_NUMBER not set");
            return;
        }

        var tsoController =
                ControllerFactoryContainerHolder.container()
                        .getTsoController(ctx.zosConnection, acct, ctx.timeout);

        String result = tsoController.issueCommand(tsoCmd);

        ctx.searchCache = new SearchCache("tso", new StringBuilder(result));
        ctx.terminal.println(result);
    }
}
