package zos.shell.service.job.processlst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.FutureUtil;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProcessLstService {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessLstService.class);

    private final JobGet jobGet;
    private final long timeout;

    public ProcessLstService(final JobGet jobGet, long timeout) {
        LOG.debug("*** ProcessLstService ***");
        this.jobGet = jobGet;
        this.timeout = timeout;
    }

    public ResponseStatus processLst(final String jobOrTask) {
        LOG.debug("*** processLst ***");
        ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        Future<ResponseStatus> submit = pool.submit(new FutureProcessListing(jobGet, jobOrTask));
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}
