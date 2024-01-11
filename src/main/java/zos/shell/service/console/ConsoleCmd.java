package zos.shell.service.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.FutureUtil;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosconsole.method.IssueConsole;

import java.util.concurrent.Executors;

public class ConsoleCmd {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleCmd.class);

    private final ZosConnection connection;
    private final long timeout;

    public ConsoleCmd(ZosConnection connection, long timeout) {
        LOG.debug("*** MvsConsoleCmd ***");
        this.connection = connection;
        this.timeout = timeout;
    }

    public ResponseStatus issueConsoleCmd(String command) {
        LOG.debug("*** issueConsoleCmd ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureConsole(connection, new IssueConsole(connection), command));
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}
