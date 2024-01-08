package zos.shell.service.job.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.FutureUtil;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.Executors;

public class DownloadJobCmd {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadJobCmd.class);

    private final JobGet retrieve;
    private final boolean isAll;
    private final long timeout;

    public DownloadJobCmd(final JobGet retrieve, boolean isAll, final long timeout) {
        LOG.debug("*** DownloadCmd ***");
        this.retrieve = retrieve;
        this.isAll = isAll;
        this.timeout = timeout;
    }

    public ResponseStatus download(final String target) {
        LOG.debug("*** download ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureDownload(retrieve, target, this.isAll, this.timeout));
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}