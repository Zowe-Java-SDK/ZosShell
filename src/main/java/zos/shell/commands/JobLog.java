package zos.shell.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.dto.ResponseStatus;
import zos.shell.future.FutureBrowseJob;
import zowe.client.sdk.zosjobs.GetJobs;
import zowe.client.sdk.zosjobs.input.GetJobParams;
import zowe.client.sdk.zosjobs.input.JobFile;
import zowe.client.sdk.zosjobs.response.Job;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class JobLog {

    private static final Logger LOG = LoggerFactory.getLogger(JobLog.class);

    protected final GetJobs getJobs;
    protected List<Job> jobs = new ArrayList<>();
    private final boolean isAll;
    private final long timeout;

    public JobLog(GetJobs getJobs, boolean isAll, long timeout) {
        LOG.debug("*** JobLog ***");
        this.getJobs = getJobs;
        this.isAll = isAll;
        this.timeout = timeout;
    }

    protected ResponseStatus browseJobLog(String param) {
        LOG.debug("*** browseJobLog ***");
        final var jobParams = new GetJobParams.Builder("*").prefix(param).build();
        try {
            jobs = getJobs.getJobsCommon(jobParams);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseStatus("error retrieving job details, try again.", false);
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
            files = getJobs.getSpoolFilesForJob(job);
        } catch (Exception e) {
            e.printStackTrace();
            final var msg = "error retrieving spool content for job id " + job.getJobId().orElse("n\\a");
            return new ResponseStatus(msg, false);
        }

        final var pool = Executors.newFixedThreadPool(1);
        final var submit = isAll ? pool.submit(new FutureBrowseJob(getJobs, files)) :
                pool.submit(new FutureBrowseJob(getJobs, List.of(files.get(0))));
        try {
            final var result = submit.get(timeout, TimeUnit.SECONDS);
            pool.shutdownNow();
            return new ResponseStatus(result.toString(), true);
        } catch (Exception e) {
            e.printStackTrace();
            pool.shutdownNow();
            return new ResponseStatus(e.getMessage(), false);
        }
    }

}