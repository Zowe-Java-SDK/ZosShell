package zos.shell.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.Constants;
import zos.shell.dto.ResponseStatus;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zostso.method.IssueTso;
import zowe.client.sdk.zostso.response.IssueResponse;

import java.util.regex.Pattern;

public class TsoCommand {

    private static final Logger LOG = LoggerFactory.getLogger(TsoCommand.class);

    private final IssueTso issueTso;
    private final String accountNumber;

    public TsoCommand(ZosConnection connection, String accountNumber) {
        LOG.debug("*** TsoCommand ***");
        this.issueTso = new IssueTso(connection);
        this.accountNumber = accountNumber;
    }

    private IssueResponse execute(String command) throws Exception {
        LOG.debug("*** execute ***");
        return issueTso.issueCommand(accountNumber, command);
    }

    public ResponseStatus executeCommand(String command) {
        LOG.debug("*** executeCommand ***");

        final var p = Pattern.compile("\"([^\"]*)\"");
        final var m = p.matcher(command);
        while (m.find()) {
            command = m.group(1);
        }

        IssueResponse response = null;
        try {
            response = execute(command);
        } catch (Exception ignored) {
        }

        if (response == null) {
            return new ResponseStatus(Constants.COMMAND_EXECUTION_ERROR_MSG, false);
        }
        return new ResponseStatus(response.getCommandResponses().orElse("no response"), true);
    }

}
