package com.commands;

import com.Constants;
import com.utility.Util;
import org.beryx.textio.TextTerminal;
import zowe.client.sdk.zosjobs.SubmitJobs;
import zowe.client.sdk.zosjobs.response.Job;

public class Submit {

    private final TextTerminal<?> terminal;
    private final SubmitJobs submitJobs;
    private Job job;

    public Submit(TextTerminal<?> terminal, SubmitJobs submitJobs) {
        this.terminal = terminal;
        this.submitJobs = submitJobs;
    }

    public void submitJob(String dataSet, String param) {
        try {
            job = submitJobs.submitJob(String.format("%s(%s)", dataSet, param));
        } catch (Exception e) {
            if (e.getMessage().contains(Constants.CONNECTION_REFUSED)) {
                terminal.println(Constants.SEVERE_ERROR);
                return;
            }
            Util.printError(terminal, e.getMessage());
        }
        if (job != null) {
            terminal.println("Job Name: " + job.getJobName().orElse("n\\a") +
                    ", Job Id: " + job.getJobId().orElse("n\\a"));
        }
    }

}
