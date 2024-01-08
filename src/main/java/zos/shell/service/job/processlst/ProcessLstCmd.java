package zos.shell.service.job.processlst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.FutureUtil;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.Executors;

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
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}
