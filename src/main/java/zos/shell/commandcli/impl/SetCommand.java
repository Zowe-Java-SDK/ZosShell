package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import zos.shell.commandcli.AbstractCommand;
import zos.shell.commandcli.CommandContext;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class SetCommand extends AbstractCommand {

    @Override
    protected String name() {
        return "set [KEY=VALE]";
    }

    @Override
    protected String description() {
        return "Set an environment variable (KEY=VALUE)";
    }

    @Override
    protected Options options() {
        // SET has no flags; expects a single KEY=VALUE argument
        return new Options();
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        if (cmd.getArgList().size() != 1) {
            ctx.terminal.println("Usage: set KEY=VALUE");
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


