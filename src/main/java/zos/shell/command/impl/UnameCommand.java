package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class UnameCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(UnameCommand.class);

    @Override
    protected String name() {
        return "uname";
    }

    @Override
    protected String description() {
        return "Display z/OS system information";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** UnameCommand.run ***");
        if (!cmd.getArgList().isEmpty()) {
            printHelp(ctx);
            return;
        }

        var ctrl = ControllerFactoryContainerHolder.container()
                .getUnameController(ctx.zosConnection, ctx.timeout);
        ctx.terminal.println(ctrl.uname(ctx.zosConnection));
    }

}
