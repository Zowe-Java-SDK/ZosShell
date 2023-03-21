package zos.shell.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.dto.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.zosconsole.ConsoleResponse;
import zowe.client.sdk.zosconsole.IssueCommand;
import zowe.client.sdk.zosconsole.input.IssueParams;

public class Terminate {

    private static Logger LOG = LoggerFactory.getLogger(Terminate.class);

    private final IssueCommand issueCommand;

    public enum Type {
        STOP,
        CANCEL
    }

    public Terminate(IssueCommand issueCommand) {
        LOG.debug("*** Terminate ***");
        this.issueCommand = issueCommand;
    }

    public ResponseStatus stopOrCancel(Type type, String jobOrTask) {
        LOG.debug("*** stopOrCancel ***");
        final var params = new IssueParams();
        switch (type) {
            case STOP:
                params.setCommand("P " + jobOrTask);
                break;
            case CANCEL:
                params.setCommand("C " + jobOrTask);
                break;
            default:
                return new ResponseStatus("invalid termination type, try again...", false);
        }
        ConsoleResponse response;
        try {
            response = issueCommand.issue(params);
            final var result = response.getCommandResponse().orElse(null);
            if (result == null) {
                return new ResponseStatus("no response from " + (type == Type.STOP ? "stop" : "cancel") + " command, try again...", false);
            }
            // remove last newline i.e. \n
            return new ResponseStatus(result.substring(0, result.length() - 1), true);
        } catch (Exception e) {
            return new ResponseStatus(Util.getErrorMsg(e + ""), false);
        }
    }

}
