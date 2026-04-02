package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.factory.ControllerFactories;

public class UnsetCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(UnsetCommand.class);

    @Override
    protected String name() {
        return "unset <NAME ...>";
    }

    @Override
    protected String[] aliases() {
        return new String[]{};
    }

    @Override
    protected String description() {
        return "Remove one or more environment variables.";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();
        LOG.debug("Executing unset for names={}", args);
        if (args.isEmpty()) {
            printHelp(ctx);
            return;
        }

        var controller = ControllerFactories
                .getGlobalFactory()
                .getEnvVariableController();

        args.forEach(name ->
                ctx.out(controller.remove(name) != null ?
                        name + " is removed." :
                        name + " does not exist."));
    }

}
