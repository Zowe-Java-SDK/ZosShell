package zos.shell.commands;

import zos.shell.future.FutureBrowseJob;
import org.beryx.textio.TextTerminal;
import zowe.client.sdk.zosjobs.GetJobs;
import zowe.client.sdk.zosjobs.input.GetJobParams;
import zowe.client.sdk.zosjobs.input.JobFile;
import zowe.client.sdk.zosjobs.response.Job;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

public class JobLog {

    protected final TextTerminal<?> terminal;
    protected final GetJobs getJobs;
    protected List<Job> jobs = new ArrayList<>();
    private final boolean isAll;
    private final long timeout;

    public JobLog(TextTerminal<?> terminal, GetJobs getJobs, boolean isAll, long timeout) {
        this.terminal = terminal;
        this.getJobs = getJobs;
        this.isAll = isAll;
        this.timeout = timeout;
    }

    protected StringBuilder browseJobLog(String param) throws Exception {
        final var jobParams = new GetJobParams.Builder("*").prefix(param).build();
        jobs = getJobs.getJobsCommon(jobParams);
        if (jobs.isEmpty()) {
            terminal.println(jobParams.getPrefix().orElse("n\\a") + " does not exist, try again...");
            return new StringBuilder();
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
            final var msg = "error retrieving spool content for job id " + job.getJobId().orElse("n\\a");
            terminal.println(msg);
            throw new Exception(e);
        }

        final var pool = Executors.newFixedThreadPool(1);
        final var submit = isAll ? pool.submit(new FutureBrowseJob(getJobs, files)) :
                pool.submit(new FutureBrowseJob(getJobs, List.of(files.get(0))));
        try {
            final var result = submit.get(timeout, TimeUnit.SECONDS);
            pool.shutdownNow();
            return result;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            pool.shutdownNow();
            throw new Exception("timeout");
        }
    }

}
