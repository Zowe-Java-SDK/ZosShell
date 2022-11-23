package zos.shell.commands;

import zos.shell.Constants;
import zos.shell.config.MvsConsoles;
import zos.shell.dto.ResponseStatus;
import org.apache.commons.lang3.SystemUtils;
import zowe.client.sdk.core.ZOSConnection;
import zowe.client.sdk.zosconsole.ConsoleResponse;
import zowe.client.sdk.zosconsole.IssueCommand;
import zowe.client.sdk.zosconsole.input.IssueParams;

import java.util.regex.Pattern;

public class MvsCommand {

    private final IssueCommand issueCommand;
    private final ZOSConnection connection;
    private final MvsConsoles mvsConsoles = new MvsConsoles();

    public MvsCommand(ZOSConnection connection) {
        this.connection = connection;
        this.issueCommand = new IssueCommand(connection);
    }

    public ResponseStatus executeCommand(String command) {
        if (!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC_OSX) {
            return new ResponseStatus(Constants.OS_ERROR, false);
        }

        final var p = Pattern.compile("\"([^\"]*)\"");
        final var m = p.matcher(command);
        while (m.find()) {
            command = m.group(1);
        }

        ConsoleResponse response = null;
        final var params = new IssueParams();
        params.setCommand(command);
        final var mvsConsoleName = mvsConsoles.getConsoleName(connection.getHost());
        if (mvsConsoleName != null) {
            params.setConsoleName(mvsConsoleName);
        }
        try {
            response = execute(params);
        } catch (Exception ignored) {
        }

        if (response == null) {
            params.setConsoleName(null);
            try {
                response = execute(params);
            } catch (Exception ignored) {
            }
        }

        if (response == null) {
            return new ResponseStatus(Constants.MVS_EXECUTION_ERROR_MSG, false);
        }
        return new ResponseStatus(Constants.MVS_EXECUTION_SUCCESS + "\n" +
                response.getCommandResponse().orElse("no response"), true);
    }

    private ConsoleResponse execute(IssueParams params) throws Exception {
        return issueCommand.issue(params);
    }

}
