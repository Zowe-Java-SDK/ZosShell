package zos.shell.service.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.FutureUtil;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosconsole.method.IssueConsole;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConsoleService {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleService.class);

    private final ZosConnection connection;
    private final long timeout;

    public ConsoleService(ZosConnection connection, long timeout) {
        LOG.debug("*** ConsoleService ***");
        this.connection = connection;
        this.timeout = timeout;
    }

    public ResponseStatus issueConsole(final String consoleName, final String command) {
        LOG.debug("*** issueConsole ***");
        ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        Future<ResponseStatus> submit = pool.submit(new FutureConsole(new IssueConsole(connection), consoleName, command));
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}
