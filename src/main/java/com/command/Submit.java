package com.command;

import com.Constants;
import com.utility.Util;
import core.ZOSConnection;
import org.beryx.textio.TextTerminal;
import zosjobs.SubmitJobs;
import zosjobs.response.Job;

public class Submit {

    private final TextTerminal<?> terminal;
    private final ZOSConnection connection;
    private SubmitJobs submitJobs;
    private Job job;

    public Submit(TextTerminal<?> terminal, ZOSConnection connection) {
        this.terminal = terminal;
        this.connection = connection;
        this.submitJobs = new SubmitJobs(connection);
    }

    public void submitJob(String dataSet, String param) {
        try {
            job = submitJobs.submitJob(String.format("%s(%s)", dataSet, param));
        } catch (Exception e) {
            if (e.getMessage().contains("Connection refused")) {
                terminal.printf(Constants.SEVERE_ERROR + "\n");
                return;
            }
            Util.printError(terminal, e.getMessage());
        }
        if (job != null)
            terminal.printf("Job Name: " + job.getJobName().orElse("n\\a") +
                    ", Job Id: " + job.getJobId().orElse("n\\a") + "\n");
    }

}