package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import zos.shell.commandcli.AbstractCommand;
import zos.shell.commandcli.CommandContext;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.service.search.SearchCache;

public class TsoCommand extends AbstractCommand {

    @Override
    protected String name() {
        return "tso \"[COMMAND_STRING]\"";
    }

    @Override
    protected String description() {
        return "Issue a TSO command";
    }

    @Override
    protected Options options() {
        // No flags; free-form TSO command text
        return new Options();
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        if (cmd.getArgList().isEmpty()) {
            ctx.terminal.println("Missing TSO command");
            return;
        }

        if (cmd.getArgList().size() != 1) {
            ctx.terminal.println("Usage: tso \"[COMMAND_STRING]\"");
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


