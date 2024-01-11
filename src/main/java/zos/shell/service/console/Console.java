package zos.shell.service.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.configuration.ConfigSingleton;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosconsole.input.IssueConsoleParams;
import zowe.client.sdk.zosconsole.method.IssueConsole;
import zowe.client.sdk.zosconsole.response.ConsoleResponse;

import java.util.Optional;
import java.util.regex.Pattern;

public class Console {

    private static final Logger LOG = LoggerFactory.getLogger(Console.class);

    private final ZosConnection connection;
    private final IssueConsole issueConsole;

    public Console(final ZosConnection connection, final IssueConsole issueConsole) {
        LOG.debug("*** MvsConsole ***");
        this.issueConsole = issueConsole;
        this.connection = connection;
    }

    public ResponseStatus issueConsole(String command) {
        LOG.debug("*** issueConsoleCmd ***");

        final var p = Pattern.compile("\"([^\"]*)\"");
        final var m = p.matcher(command);
        while (m.find()) {
            command = m.group(1);
        }

        ConsoleResponse consoleResponse;
        final var params = new IssueConsoleParams(command);
        final var configSettings = ConfigSingleton.getInstance().getConfigSettings();
        final var consoleName = Optional.ofNullable(configSettings != null && configSettings.getConsoleName() != null ?
                configSettings.getConsoleName() : null);
        try {
            consoleResponse = consoleName.isPresent() ? execute(consoleName.get(), params) : execute(params);
        } catch (ZosmfRequestException e) {
            final var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
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
