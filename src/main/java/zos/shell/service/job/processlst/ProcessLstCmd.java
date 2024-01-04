package zos.shell.service.job.processlst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ProcessLstCmd {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessLstCmd.class);

    private final JobGet jobGet;
    private final long timeout;

    public ProcessLstCmd(final JobGet jobGet, long timeout) {
        LOG.debug("*** ProcessLstCmd ***");
        this.jobGet = jobGet;
        this.timeout = timeout;
    }

    public ResponseStatus processLst(final String jobOrTask) {
        LOG.debug("*** processLst ***");

        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureProcessLst(jobGet, jobOrTask));

        try {
            return submit.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            submit.cancel(true);
            LOG.debug("error: " + e);
            return new ResponseStatus(Constants.TIMEOUT_MESSAGE, false);
        } finally {
            pool.shutdown();
        }
    }

}
