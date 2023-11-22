package zos.shell.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.Constants;
import zos.shell.config.MvsConsoles;
import zos.shell.dto.ResponseStatus;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosconsole.input.IssueConsoleParams;
import zowe.client.sdk.zosconsole.method.IssueConsole;
import zowe.client.sdk.zosconsole.response.ConsoleResponse;

import java.util.regex.Pattern;

public class MvsCommand {

    private static final Logger LOG = LoggerFactory.getLogger(MvsCommand.class);

    private final IssueConsole issueConsole;
    private final ZosConnection connection;
    private final MvsConsoles mvsConsoles = new MvsConsoles();

    public MvsCommand(ZosConnection connection) {
        LOG.debug("*** MvsCommand ***");
        this.connection = connection;
        this.issueConsole = new IssueConsole(connection);
    }

    private ConsoleResponse execute(IssueConsoleParams params) throws Exception {
        LOG.debug("*** execute ***");
        return issueConsole.issueCommand(params.getCmd().get());
    }

    private ConsoleResponse execute(String consoleName, IssueConsoleParams params) throws Exception {
        LOG.debug("*** execute ***");
        return issueConsole.issueCommandCommon(consoleName, params);
    }

    public ResponseStatus executeCommand(String command) {
        LOG.debug("*** executeCommand ***");

        final var p = Pattern.compile("\"([^\"]*)\"");
        final var m = p.matcher(command);
        while (m.find()) {
            command = m.group(1);
        }

        ConsoleResponse response = null;
        final var params = new IssueConsoleParams(command);
        final var consoleName = mvsConsoles.getConsoleName(connection.getHost());
        try {
            if (consoleName != null) {
                response = execute(consoleName, params);
            }
            response = execute(params);
        } catch (Exception ignored) {
        }

        if (response == null) {
            return new ResponseStatus(Constants.COMMAND_EXECUTION_ERROR_MSG, false);
        }
        return new ResponseStatus(Constants.MVS_EXECUTION_SUCCESS + "\n" +
                response.getCommandResponse().orElse("no response"), true);
    }

}
