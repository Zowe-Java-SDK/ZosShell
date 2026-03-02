package zos.shell.service.dsn.makedir;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosfiles.dsn.input.DsnCreateInputData;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCreate;

import java.util.concurrent.*;

public class MakeDirService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(MakeDirService.class);

    private final DsnCreate dsnCreate;
    private final long timeout;
    private final ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);

    public MakeDirService(final DsnCreate dsnCreate, final long timeout) {
        LOG.debug("*** MakeDirService ***");
        this.dsnCreate = dsnCreate;
        this.timeout = timeout;
    }

    public ResponseStatus create(final String dataset, final DsnCreateInputData params) {
        LOG.debug("*** create ***");
        Future<ResponseStatus> future = pool.submit(new FutureMakeDirectory(
                dsnCreate,
                dataset,
                params
        ));

        try {
            return future.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            LOG.debug("Exception in MakeDirService", e);
            future.cancel(true);
            return new ResponseStatus(getErrorMessage(e), false);
        } catch (TimeoutException e) {
            LOG.debug("Timeout in MakeDirService", e);
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
