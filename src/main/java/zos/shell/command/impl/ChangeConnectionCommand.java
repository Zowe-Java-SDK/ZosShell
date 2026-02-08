package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.constants.Constants;
import zos.shell.controller.container.ControllerFactoryContainerHolder;
import zos.shell.singleton.ConnSingleton;
import zos.shell.singleton.TerminalSingleton;
import zos.shell.singleton.configuration.ConfigSingleton;

public class ChangeConnectionCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeConnectionCommand.class);

    @Override
    protected String name() {
        return "change <INDEX_NUM>";
    }

    @Override
    protected String[] aliases() {
        return new String[]{};
    }

    @Override
    protected String description() {
        return "Change the current z/OS connection";
    }


    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** ChangeConnectionCommand.run ***");
        var args = cmd.getArgList();
        if (args.size() != 1) {
            printHelp(ctx);
            return;
        }

        int changeIndex;
        try {
            int num = Integer.parseInt(args.get(0));
            changeIndex = num != 0 ? num - 1 : 0;
            var numOfConnections = ConfigSingleton.getInstance().getZosConnections().size() - 1;
            if (changeIndex < 0 || changeIndex > numOfConnections) {
                ctx.terminal.println(Constants.NO_CONNECTION);
                return;
            }
        } catch (NumberFormatException e) {
            ctx.terminal.println("Invalid index");
            return;
        }

        var changeConnController = ControllerFactoryContainerHolder.container()
                .getChangeConnController(ctx.terminal);
        ctx.zosConnection = changeConnController.changeZosConnection(ctx.zosConnection, changeIndex);
        ConnSingleton.getInstance().setCurrZosConnection(ctx.zosConnection, changeIndex);
        ctx.sshConnection = changeConnController.changeSshConnection(ctx.sshConnection, changeIndex);
        ConnSingleton.getInstance().setCurrSshConnection(ctx.sshConnection);
        ctx.currZosConnectionIndex = changeIndex;

        TerminalSingleton.getInstance().getMainTerminal()
                .setPaneTitle(Constants.APP_TITLE + " - " + ctx.zosConnection.getHost().toUpperCase());
        var msg = String.format("Connection changed:\nhost:%s\nuser:%s\nzosmfport:%s\nsshport:%s",
                ctx.zosConnection.getHost(), ctx.zosConnection.getUser(), ctx.zosConnection.getZosmfPort(),
                ctx.sshConnection.getPort());
        ctx.terminal.println(msg);
    }

}
