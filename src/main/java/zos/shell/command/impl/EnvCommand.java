package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.service.search.SearchCache;

public class EnvCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(EnvCommand.class);

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
        LOG.debug("*** EnvCommand.run ***");
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
