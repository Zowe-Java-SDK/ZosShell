package com.commands;

import com.Constants;
import com.utility.Util;
import core.ZOSConnection;
import org.beryx.textio.TextTerminal;
import zosjobs.GetJobs;
import zosjobs.input.GetJobParams;
import zosjobs.response.Job;

import java.util.Comparator;
import java.util.List;

public class ProcessList {

    private final TextTerminal<?> terminal;
    private final GetJobs getJobs;
    private final GetJobParams.Builder getJobParams = new GetJobParams.Builder("*");

    public ProcessList(TextTerminal<?> terminal, ZOSConnection connection) {
        this.terminal = terminal;
        this.getJobs = new GetJobs(connection);
    }

    public void ps(String task) {
        List<Job> jobs;
        try {
            if (task != null) {
                getJobParams.prefix(task).build();
            }
            var params = getJobParams.build();
            jobs = getJobs.getJobsCommon(params);
        } catch (Exception e) {
            if (e.getMessage().contains("Connection refused")) {
                terminal.println(Constants.SEVERE_ERROR);
                return;
            }
            Util.printError(terminal, e.getMessage());
            return;
        }
        jobs.sort(Comparator.comparing((Job j) -> j.getJobName().orElse(""))
                .thenComparing(j -> j.getStatus().orElse(""))
                .thenComparing(j -> j.getJobId().orElse("")));
        if (jobs.isEmpty()) {
            terminal.println(Constants.NO_PROCESS_FOUND);
            return;
        }
        jobs.forEach(job -> {
            var jobName = job.getJobName().orElse("");
            var jobId = job.getJobId().orElse("");
            var jobStatus = job.getStatus().orElse("");
            terminal.println(String.format("%-8s %-8s %-8s", jobName, jobId, jobStatus));
        });
    }

}
