package zos.shell.service.job.purge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosjobs.methods.JobDelete;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PurgeCmd {

    private static final Logger LOG = LoggerFactory.getLogger(PurgeCmd.class);

    private final JobDelete delete;
    private final JobGet retrieve;
    private final long timeout;

    public PurgeCmd(final JobDelete delete, final JobGet retrieve, final long timeout) {
        LOG.debug("*** PurgeCmd ***");
        this.delete = delete;
        this.retrieve = retrieve;
        this.timeout = timeout;
    }

    public ResponseStatus purge(String filter) {
        LOG.debug("*** purge ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FuturePurge(delete, retrieve, filter));

        try {
            return submit.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            LOG.debug("error: " + e);
            submit.cancel(true);
            return new ResponseStatus(e.getMessage() != null && !e.getMessage().isBlank() ?
                    e.getMessage() : Constants.COMMAND_EXECUTION_ERROR_MSG, false);
        } catch (TimeoutException e) {
            submit.cancel(true);
            return new ResponseStatus(Constants.TIMEOUT_MESSAGE, false);
        } finally {
            pool.shutdown();
        }
    }

}
