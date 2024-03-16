package zos.shell.service.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosconsole.input.IssueConsoleParams;
import zowe.client.sdk.zosconsole.method.IssueConsole;
import zowe.client.sdk.zosconsole.response.ConsoleResponse;

import java.util.regex.Pattern;

public class Console {

    private static final Logger LOG = LoggerFactory.getLogger(Console.class);

    private final IssueConsole issueConsole;
    private final String consoleName;

    public Console(final IssueConsole issueConsole, final String consoleName) {
        LOG.debug("*** Console ***");
        this.issueConsole = issueConsole;
        this.consoleName = consoleName;
    }

    public ResponseStatus issueConsole(String command) {
        LOG.debug("*** issueConsoleCmd ***");

        var p = Pattern.compile("\"([^\"]*)\"");
        var m = p.matcher(command);
        while (m.find()) {
            command = m.group(1);
        }

        ConsoleResponse consoleResponse;
        var params = new IssueConsoleParams(command);
        params.setProcessResponse(true);
        try {
            consoleResponse = !consoleName.isBlank() ? execute(consoleName, params) : execute(params);
        } catch (ZosmfRequestException e) {
            var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }

        return new ResponseStatus(Constants.MVS_EXECUTION_SUCCESS + "\n" +
                consoleResponse.getCommandResponse().orElse("no data"), true);
    }

    private ConsoleResponse execute(final IssueConsoleParams params) throws ZosmfRequestException {
        LOG.debug("*** execute ***");
        return issueConsole.issueCommand(params.getCmd()
                .orElseThrow(() -> new ZosmfRequestException("no command value specified")));
    }

    private ConsoleResponse execute(final String consoleName, final IssueConsoleParams params)
            throws ZosmfRequestException {
        LOG.debug("*** execute ***");
        return issueConsole.issueCommandCommon(consoleName, params);
    }

}
