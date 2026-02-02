package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class SetCommand extends NoOptionCommand {

    @Override
    protected String name() {
        return "set <KEY=VALE>";
    }

    @Override
    protected String description() {
        return "Set an environment variable (KEY=VALUE)";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        if (cmd.getArgList().size() != 1) {
            printHelp(ctx);
            return;
        }

        String kv = cmd.getArgList().get(0);

        if (!kv.contains("=")) {
            ctx.terminal.println("Invalid SET syntax. Expected KEY=VALUE");
            return;
        }

        var env = ControllerFactoryContainerHolder
                .container()
                .getEnvVariableController();

        ctx.terminal.println(env.set(kv));
    }
}
