package com.commands;

import com.Constants;
import com.future.FutureBrowseJob;
import org.beryx.textio.TextTerminal;
import zowe.client.sdk.zosjobs.GetJobs;
import zowe.client.sdk.zosjobs.input.GetJobParams;
import zowe.client.sdk.zosjobs.input.JobFile;
import zowe.client.sdk.zosjobs.response.Job;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

public class JobLog {

    protected final TextTerminal<?> terminal;
    protected final GetJobs getJobs;
    private final boolean isAll;

    public JobLog(TextTerminal<?> terminal, GetJobs getJobs, boolean isAll) {
        this.terminal = terminal;
        this.getJobs = getJobs;
        this.isAll = isAll;
    }

    protected StringBuilder browseJobLog(String param) throws Exception {
        var jobParams = new GetJobParams.Builder("*").prefix(param).build();
        final var jobs = getJobs.getJobsCommon(jobParams);
        if (jobs.isEmpty()) {
            terminal.println(jobParams.getPrefix().orElse("n\\a") + " does not exist, try again...");
            return new StringBuilder();
        }
        // select the active or input one first; if not found then get the highest job number
        Predicate<Job> isActive = j -> "ACTIVE".equalsIgnoreCase(j.getStatus().orElse(""));
        Predicate<Job> isInput = j -> "INPUT".equalsIgnoreCase(j.getStatus().orElse(""));

        var jobStillRunning = jobs.stream().filter(isActive.or(isInput)).findAny();
        var job = jobStillRunning.orElse(jobs.get(0));
        final List<JobFile> files;
        try {
            files = getJobs.getSpoolFilesForJob(job);
        } catch (Exception e) {
            var msg = "error retrieving spool content for job id " + job.getJobId().orElse("n\\a");
            terminal.println(msg);
            throw new Exception(e);
        }

        var pool = Executors.newFixedThreadPool(1);
        var result = isAll ? pool.submit(new FutureBrowseJob(getJobs, files)) :
                pool.submit(new FutureBrowseJob(getJobs, List.of(files.get(0))));
        try {
            pool.shutdown();
            return result.get(Constants.FUTURE_TIMEOUT_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            pool.shutdown();
            throw new Exception("timeout");
        }
    }

}
