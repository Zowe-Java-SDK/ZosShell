package com.commands;

import org.beryx.textio.TextTerminal;
import zosjobs.GetJobs;
import zosjobs.input.GetJobParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JobLog {

    protected final TextTerminal<?> terminal;
    protected final GetJobs getJobs;
    private final boolean isAll;

    public JobLog(TextTerminal<?> terminal, GetJobs getJobs, boolean isAll) {
        this.terminal = terminal;
        this.getJobs = getJobs;
        this.isAll = isAll;
    }

    protected List<String> browseJobLog(String param) throws Exception {
        var jobParams = new GetJobParams.Builder("*").prefix(param).build();
        final var jobs = getJobs.getJobsCommon(jobParams);
        if (jobs.isEmpty()) {
            terminal.println(jobParams.getPrefix().orElse("n\\a") + " does not exist, try again...");
            return new ArrayList<>();
        }
        // select the active one first not found then get the highest job number
        var job = jobs.stream().filter(j -> "ACTIVE".equalsIgnoreCase(j.getStatus().orElse(""))).findAny();
        final var files = getJobs.getSpoolFilesForJob(job.orElse(jobs.get(0)));

        if (!isAll) {
            return Arrays.asList(getJobs.getSpoolContent(files.get(0)).split("\n"));
        }

        var pool = Executors.newFixedThreadPool(1);
        var result = pool.submit(new BrowseJobAll(getJobs, files));
        try {
            return result.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new Exception("timeout");
        }
    }

}
