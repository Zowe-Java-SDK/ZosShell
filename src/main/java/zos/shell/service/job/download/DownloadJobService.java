package zos.shell.service.job.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.path.PathService;
import zos.shell.utility.FutureUtil;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DownloadJobService {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadJobService.class);

    private final JobGet retrieve;
    private final PathService pathService;
    private final boolean isAll;
    private final long timeout;

    public DownloadJobService(final JobGet retrieve, final PathService pathService, final boolean isAll,
                              final long timeout) {
        LOG.debug("*** DownloadJobService ***");
        this.retrieve = retrieve;
        this.pathService = pathService;
        this.isAll = isAll;
        this.timeout = timeout;
    }

    public ResponseStatus download(final String target, final String jobId) {
        LOG.debug("*** download ***");
        ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        Future<ResponseStatus> submit = pool.submit(
                new FutureDownloadJob(retrieve, pathService, target, this.isAll, jobId, this.timeout));
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}