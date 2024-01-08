package zos.shell.service.job.purge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.FutureUtil;
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
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}
