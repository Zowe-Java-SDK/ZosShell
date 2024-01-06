package zos.shell.service.job.submit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosjobs.methods.JobSubmit;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SubmitCmd {

    private static final Logger LOG = LoggerFactory.getLogger(SubmitCmd.class);

    private final JobSubmit submit;
    private final long timeout;

    public SubmitCmd(final JobSubmit submit, final long timeout) {
        LOG.debug("*** SubmitCmd ***");
        this.submit = submit;
        this.timeout = timeout;
    }

    public ResponseStatus submit(final String dataset, final String target) {
        LOG.debug("*** submit ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureSubmit(this.submit, dataset, target));

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
