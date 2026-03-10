package zos.shell.service.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.FutureUtil;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosconsole.method.ConsoleCmd;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConsoleService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ConsoleService.class);

    private final ZosConnection connection;
    private final long timeout;
    private final ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);

    public ConsoleService(final ZosConnection connection, final long timeout) {
        LOG.debug("*** ConsoleService ***");
        this.connection = connection;
        this.timeout = timeout;
    }

    public ResponseStatus issueConsole(final String consoleName, final String command) {
        LOG.debug("Issuing console command '{}' on '{}'", command, consoleName);
        Future<ResponseStatus> future = pool.submit(new FutureConsole(
                new ConsoleCmd(connection),
                consoleName,
                command
        ));
        return FutureUtil.waitForResult(future, timeout);
    }

    @Override
    public void close() {
        pool.shutdown();
    }

}
