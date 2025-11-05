package zos.shell.service.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosconsole.ConsoleConstants;
import zowe.client.sdk.zosconsole.input.ConsoleCmdInputData;
import zowe.client.sdk.zosconsole.method.ConsoleCmd;
import zowe.client.sdk.zosconsole.response.ConsoleCmdResponse;

import java.util.regex.Pattern;

public class Console {

    private static final Logger LOG = LoggerFactory.getLogger(Console.class);

    private final ConsoleCmd issueConsole;
    private final String consoleName;

    public Console(final ConsoleCmd issueConsole, final String consoleName) {
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

        ConsoleCmdResponse consoleResponse;
        var params = new ConsoleCmdInputData(command);
        params.setProcessResponse();
        try {
            consoleResponse = !(consoleName == null || consoleName.isBlank()) ?
                    execute(consoleName, params) : execute(params);
        } catch (ZosmfRequestException e) {
            var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }

        return new ResponseStatus(Constants.MVS_EXECUTION_SUCCESS + "\n" +
                consoleResponse.getCmdResponse(), true);
    }

    private ConsoleCmdResponse execute(final ConsoleCmdInputData params) throws ZosmfRequestException {
        LOG.debug("*** execute issue common command with default consoleName ***");
        return issueConsole.issueCommandCommon(ConsoleConstants.RES_DEF_CN, params);
    }

    private ConsoleCmdResponse execute(final String consoleName, final ConsoleCmdInputData params)
            throws ZosmfRequestException {
        LOG.debug("*** execute issue common command with consoleName ***");
        return issueConsole.issueCommandCommon(consoleName, params);
    }

}
