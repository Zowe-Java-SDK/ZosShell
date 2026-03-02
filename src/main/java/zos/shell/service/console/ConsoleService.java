package zos.shell.service.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosconsole.method.ConsoleCmd;

import java.util.concurrent.*;

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

        try {
            return future.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            LOG.debug("Exception console service", e);
            future.cancel(true);
            return new ResponseStatus(getErrorMessage(e), false);
        } catch (TimeoutException e) {
            LOG.debug("Timeout console service", e);
            future.cancel(true);
            return new ResponseStatus(Constants.TIMEOUT_MESSAGE, false);
        }
    }

    @Override
    public void close() {
        pool.shutdown();
    }

    private String getErrorMessage(final Exception e) {
        LOG.debug("*** getErrorMessage ***");
        return e.getMessage() != null && !e.getMessage().isBlank()
                ? e.getMessage()
                : Constants.COMMAND_EXECUTION_ERROR_MSG;
    }

}
