package zos.shell.service.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.configuration.MvsConsoles;
import zos.shell.dto.Output;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosconsole.input.IssueConsoleParams;
import zowe.client.sdk.zosconsole.method.IssueConsole;
import zowe.client.sdk.zosconsole.response.ConsoleResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

public class MvsConsoleCmd {

    private static final Logger LOG = LoggerFactory.getLogger(MvsConsoleCmd.class);

    private final ZosConnection connection;
    private final long timeout;

    public MvsConsoleCmd(ZosConnection connection, long timeout) {
        LOG.debug("*** MvsConsoleCmd ***");
        this.connection = connection;
        this.timeout = timeout;
    }

    public ResponseStatus issueConsoleCmd(String command) {
        LOG.debug("*** issueConsoleCmd ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureMvs(connection, new IssueConsole(connection), command));

        try {
            final var responseStatus = submit.get(timeout, TimeUnit.SECONDS);
            return new ResponseStatus(responseStatus.getMessage(), responseStatus.isStatus());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return new ResponseStatus(e.getMessage(), false);
        }
    }

}
