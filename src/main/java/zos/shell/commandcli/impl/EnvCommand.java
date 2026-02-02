package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.service.search.SearchCache;

public class EnvCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "env";
    }

    @Override
    protected String description() {
        return "Display environment variables";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        if (!cmd.getArgList().isEmpty()) {
            printHelp(ctx);
            return;
        }

        String result = ControllerFactoryContainerHolder.container()
                .getEnvVariableController()
                .env();

        ctx.terminal.println(result);
        ctx.searchCache = new SearchCache("env", new StringBuilder(result));
    }

}
