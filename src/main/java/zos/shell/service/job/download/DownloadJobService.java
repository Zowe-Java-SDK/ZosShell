package zos.shell.service.job.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.path.PathService;
import zos.shell.utility.FutureResponseUtil;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DownloadJobService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadJobService.class);

    private final JobGet retrieve;
    private final PathService pathService;
    private final boolean isAll;
    private final String jobId;
    private final long timeout;
    private final ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);

    public DownloadJobService(final JobGet retrieve,
                              final PathService pathService,
                              final boolean isAll,
                              final String jobId,
                              final long timeout) {
        LOG.debug("*** DownloadJobService ***");
        this.retrieve = retrieve;
        this.pathService = pathService;
        this.isAll = isAll;
        this.jobId = jobId;
        this.timeout = timeout;
    }

    public ResponseStatus download(final String target) {
        LOG.info("Starting download job for '{}'", target);
        Future<ResponseStatus> future = pool.submit(new FutureDownloadJob(
                retrieve,
                pathService,
                target,
                this.isAll,
                this.jobId,
                this.timeout
        ));
        return FutureResponseUtil.waitForResult(future, timeout);
    }

    @Override
    public void close() {
        pool.shutdown();
    }

}
