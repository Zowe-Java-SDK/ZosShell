package zos.shell.service.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.configuration.MvsConsoles;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosconsole.input.IssueConsoleParams;
import zowe.client.sdk.zosconsole.method.IssueConsole;
import zowe.client.sdk.zosconsole.response.ConsoleResponse;

import java.util.regex.Pattern;

public class MvsCmd {

    private static final Logger LOG = LoggerFactory.getLogger(MvsCmd.class);

    private final IssueConsole issueConsole;
    private final ZosConnection connection;
    private final MvsConsoles mvsConsoles = new MvsConsoles();

    public MvsCmd(ZosConnection connection) {
        LOG.debug("*** MvsCommand ***");
        this.connection = connection;
        this.issueConsole = new IssueConsole(connection);
    }

    private ConsoleResponse execute(IssueConsoleParams params) throws ZosmfRequestException {
        LOG.debug("*** execute ***");
        return issueConsole.issueCommand(params.getCmd().get());
    }

    private ConsoleResponse execute(String consoleName, IssueConsoleParams params) throws ZosmfRequestException {
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

        ConsoleResponse response;
        final var params = new IssueConsoleParams(command);
        final var consoleName = mvsConsoles.getConsoleName(connection.getHost());
        try {
            if (consoleName != null) {
                response = execute(consoleName, params);
            } else {
                response = execute(params);
            }
        } catch (ZosmfRequestException e) {
            final String errMsg = Util.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }

        return new ResponseStatus(Constants.MVS_EXECUTION_SUCCESS + "\n" +
                response.getCommandResponse().orElse("no response"), true);
    }

}
