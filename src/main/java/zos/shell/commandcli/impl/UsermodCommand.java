package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import zos.shell.commandcli.AbstractCommand;
import zos.shell.commandcli.CommandContext;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

public class UsermodCommand extends AbstractCommand {

    @Override
    protected String name() {
        return "usermod";
    }

    @Override
    protected String[] aliases() {
        return new String[]{};
    }

    @Override
    protected String description() {
        return "Modify the current user settings";
    }

    @Override
    protected String usage() {
        return "usermod <OPTION>";
    }

    @Override
    protected Options options() {
        Options opts = new Options();
        opts.addOption(Option.builder("u")
                .longOpt("user")
                .desc("Modify the user name")
                .build());
        opts.addOption(Option.builder("p")
                .longOpt("password")
                .desc("Modify the password")
                .build());
        return opts;
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        if (!cmd.hasOption("u") && !cmd.hasOption("p")) {
            printHelp(ctx);
            return;
        }

        var controller = ControllerFactoryContainerHolder.container()
                .getUsermodController(ctx.zosConnection, ctx.currZosConnectionIndex);

        String result = "";

        if (cmd.hasOption("u")) {
            result += controller.change("-u") + "\n";
        }
        if (cmd.hasOption("p")) {
            result += controller.change("-p") + "\n";
        }

        ctx.terminal.println(result.trim());
    }
}

