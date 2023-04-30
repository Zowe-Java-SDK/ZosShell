package zos.shell.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.Constants;
import zos.shell.dto.ResponseStatus;
import zowe.client.sdk.core.ZOSConnection;
import zowe.client.sdk.zostso.IssueResponse;
import zowe.client.sdk.zostso.IssueTso;

import java.util.regex.Pattern;

public class TsoCommand {

    private static final Logger LOG = LoggerFactory.getLogger(TsoCommand.class);

    private final IssueTso issueCommand;
    private final String accountNumber;

    public TsoCommand(ZOSConnection connection, String accountNumber) {
        LOG.debug("*** TsoCommand ***");
        this.issueCommand = new IssueTso(connection);
        this.accountNumber = accountNumber;
    }

    private IssueResponse execute(String command) throws Exception {
        LOG.debug("*** execute ***");
        return issueCommand.issueTsoCommand(accountNumber, command);
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
