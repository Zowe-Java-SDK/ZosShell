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
    private final ZOSConnection connection;

    public ProcessList(TextTerminal<?> terminal, ZOSConnection connection) {
        this.terminal = terminal;
        this.connection = connection;
    }

    public void ps(String task) {
        List<Job> jobs;
        try {
            final var getJobs = new GetJobs(connection);
            GetJobParams.Builder getJobParams = new GetJobParams.Builder("*");
            if (task != null) {
                getJobParams.prefix(task).build();
            }
            var params = getJobParams.build();
            jobs = getJobs.getJobsCommon(params);
        } catch (Exception e) {
            if (e.getMessage().contains("Connection refused")) {
                terminal.printf(Constants.SEVERE_ERROR + "\n");
                return;
            }
            Util.printError(terminal, e.getMessage());
            return;
        }
        jobs.sort(Comparator.comparing((Job j) -> j.getJobName().get())
                .thenComparing(j -> j.getStatus().get()).thenComparing(j -> j.getJobId().get()));
        jobs.forEach(job -> terminal.printf(
                String.format("%-8s %-8s %-8s\n", job.getJobName().get(), job.getJobId().get(), job.getStatus().get()))
        );
    }

}
