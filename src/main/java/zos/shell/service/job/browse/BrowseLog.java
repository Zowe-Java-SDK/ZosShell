package zos.shell.service.job.browse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosjobs.input.GetJobParams;
import zowe.client.sdk.zosjobs.input.JobFile;
import zowe.client.sdk.zosjobs.methods.JobGet;
import zowe.client.sdk.zosjobs.response.Job;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

public class BrowseLog {

    private static final Logger LOG = LoggerFactory.getLogger(BrowseLog.class);

    protected final JobGet retrieve;
    public List<Job> jobs = new ArrayList<>();
    private final boolean isAll;
    private final long timeout;

    public BrowseLog(final JobGet retrieve, boolean isAll, final long timeout) {
        LOG.debug("*** BrowseLog ***");
        this.retrieve = retrieve;
        this.isAll = isAll;
        this.timeout = timeout;
    }

    protected ResponseStatus browseLog(final String target) {
        LOG.debug("*** browseLog ***");
        final var jobParams = new GetJobParams.Builder("*").prefix(target).build();
        try {
            jobs = retrieve.getCommon(jobParams);
        } catch (ZosmfRequestException e) {
            final String errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }
        if (jobs.isEmpty()) {
            final var msg = jobParams.getPrefix().orElse("n\\a") + " does not exist, try again...";
            return new ResponseStatus(msg, false);
        }
        // select the active or input one first; if not found then get the highest job number
        final Predicate<Job> isActive = j -> "ACTIVE".equalsIgnoreCase(j.getStatus().orElse(""));
        final Predicate<Job> isInput = j -> "INPUT".equalsIgnoreCase(j.getStatus().orElse(""));

        final var jobStillRunning = jobs.stream().filter(isActive.or(isInput)).findAny();
        final var job = jobStillRunning.orElse(jobs.get(0));
        final List<JobFile> files;
        try {
            files = retrieve.getSpoolFilesByJob(job);
        } catch (ZosmfRequestException e) {
            final String msg = ResponseUtil.getResponsePhrase(e.getResponse());
            final var errMsg = "error retrieving spool content for job id " + job.getJobId().orElse("n\\a") +
                    ", " + (msg != null ? msg : e.getMessage());
            return new ResponseStatus(errMsg, false);
        }

        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = isAll ? pool.submit(new FutureBrowseLog(retrieve, files)) :
                pool.submit(new FutureBrowseLog(retrieve, List.of(files.get(0))));
        try {
            final var result = submit.get(timeout, TimeUnit.SECONDS);
            return new ResponseStatus(result.toString(), true);
        } catch (ExecutionException | InterruptedException e) {
            submit.cancel(true);
            return new ResponseStatus(e.getMessage() != null && !e.getMessage().isBlank() ?
                    e.getMessage() : Constants.COMMAND_EXECUTION_ERROR_MSG, false);
        } catch (TimeoutException e) {
            submit.cancel(true);
            return new ResponseStatus(Constants.TIMEOUT_MESSAGE, false);
        } finally {
            pool.shutdownNow();
        }
    }

}