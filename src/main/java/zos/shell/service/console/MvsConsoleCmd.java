package zos.shell.service.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosconsole.method.IssueConsole;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
            return submit.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return new ResponseStatus(e.getMessage(), false);
        }
    }

}
