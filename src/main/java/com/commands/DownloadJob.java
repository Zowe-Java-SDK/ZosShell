package com.commands;

import com.Constants;
import com.google.common.base.Strings;
import com.utility.DirectorySetup;
import com.utility.Util;
import org.beryx.textio.TextTerminal;
import zowe.client.sdk.zosjobs.GetJobs;

import java.io.IOException;

public class DownloadJob {

    private final TextTerminal<?> terminal;
    private final BrowseJob browseJob;

    public DownloadJob(TextTerminal<?> terminal, GetJobs getJobs, boolean isAll, long timeOutValue) {
        this.terminal = terminal;
        this.browseJob = new BrowseJob(terminal, getJobs, isAll, timeOutValue);
    }

    public void download(String jobName) {
        var error = "error retrieving " + jobName + " log ";
        StringBuilder output;
        try {
            output = browseJob.browseJob(jobName);
        } catch (Exception e) {
            terminal.println(error + e.getMessage());
            return;
        }
        if (this.browseJob.jobs.isEmpty()) {
            terminal.println(error);
            return;
        }
        var jobId = this.browseJob.jobs.get(0).getJobId().orElse(null);

        DirectorySetup dirSetup = new DirectorySetup();
        try {
            dirSetup.initialize(jobName, jobId);
        } catch (Exception e) {
            terminal.println(error + e.getMessage());
            return;
        }

        try {
            Util.writeTextFile(output.toString(), dirSetup.getDirectoryPath(), dirSetup.getFileNamePath());
        } catch (IOException e) {
            terminal.println(error + e.getMessage());
            return;
        }

        var message = Strings.padStart(jobName, 8, ' ') + Constants.ARROW;
        terminal.println(message + "downloaded to " + dirSetup.getFileNamePath());
    }

}
