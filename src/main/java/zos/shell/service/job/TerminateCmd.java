package zos.shell.service.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.configuration.MvsConsoles;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosconsole.input.IssueConsoleParams;
import zowe.client.sdk.zosconsole.method.IssueConsole;
import zowe.client.sdk.zosconsole.response.ConsoleResponse;

public class TerminateCmd {

    private static final Logger LOG = LoggerFactory.getLogger(TerminateCmd.class);

    private final IssueConsole issueConsole;
    private final MvsConsoles mvsConsoles = new MvsConsoles();
    private final ZosConnection connection;

    public enum Type {
        STOP,
        CANCEL
    }

    public TerminateCmd(ZosConnection connection, IssueConsole issueConsole) {
        LOG.debug("*** Terminate ***");
        this.connection = connection;
        this.issueConsole = issueConsole;
    }

    public ResponseStatus stopOrCancel(Type type, String jobOrTask) {
        LOG.debug("*** stopOrCancel ***");
        IssueConsoleParams params;
        switch (type) {
            case STOP:
                params = new IssueConsoleParams("P " + jobOrTask);
                break;
            case CANCEL:
                params = new IssueConsoleParams("C " + jobOrTask);
                break;
            default:
                return new ResponseStatus("invalid termination type, try again...", false);
        }
        ConsoleResponse response;
        try {
            final var consoleName = mvsConsoles.getConsoleName(connection.getHost());
            if (consoleName != null) {
                response = issueConsole.issueCommandCommon(consoleName, params);
            } else {
                response = issueConsole.issueCommand(params.getCmd().get());
            }
            final var result = response.getCommandResponse().orElse(null);
            if (result == null) {
                final var errMsg = "no response from " + (type == Type.STOP ? "stop" : "cancel") + " command, try again...";
                return new ResponseStatus(errMsg, false);
            }
            // remove last newline i.e. \n
            return new ResponseStatus(result.substring(0, result.length() - 1), true);
        } catch (ZosmfRequestException e) {
            final String errMsg = Util.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }
    }

}