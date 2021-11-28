package com.commands;

import com.Constants;
import com.utility.Util;
import core.ZOSConnection;
import org.beryx.textio.TextTerminal;
import zosjobs.GetJobs;
import zosjobs.input.GetJobParams;
import zosjobs.response.Job;

import java.util.Optional;

public class GetJobOutput {

    private final TextTerminal<?> terminal;
    private final GetJobParams.Builder jobParams = new GetJobParams.Builder("*");
    private final GetJobs getJobs;

    public GetJobOutput(TextTerminal<?> terminal, ZOSConnection connection) {
        this.terminal = terminal;
        this.getJobs = new GetJobs(connection);
    }

    public String[] getLog(String param) {
        String[] output;
        try {
            output = getJobLog(getJobs, jobParams.prefix(param).build());
        } catch (Exception e) {
            if (e.getMessage().contains("Connection refused")) {
                terminal.println(Constants.SEVERE_ERROR);
                return null;
            }
            Util.printError(terminal, e.getMessage());
            return null;
        }
        return output;
    }

    public void tail(String[] params) {
        var output = getLog(params[1]);
        if (output == null) return;
        var size = output.length;
        var lines = 0;
        if (params.length == 3) {
            try {
                lines = Integer.parseInt(params[2]);
            } catch (NumberFormatException e) {
                terminal.println(Constants.INVALID_PARAMETER);
                return;
            }
        }

        if (lines > 0) {
            if (lines < size) {
                for (int i = size - lines; i < size; i++)
                    terminal.println(output[i]);
            } else {
                printAll(output, size);
            }
        } else {
            int LINES_LIMIT = 25;
            if (size > LINES_LIMIT) {
                for (int i = size - LINES_LIMIT; i < size; i++)
                    terminal.println(output[i]);
            } else {
                printAll(output, size);
            }
        }
    }

    private String[] getJobLog(GetJobs getJobs, GetJobParams jobParams) throws Exception {
        final var jobs = getJobs.getJobsCommon(jobParams);
        // select the active one first not found then get the highest job number
        Optional<Job> job = jobs.stream().filter(j -> "ACTIVE".equalsIgnoreCase(j.getStatus().orElse(""))).findAny();
        final var files = getJobs.getSpoolFilesForJob(job.orElse(jobs.get(0)));
        return getJobs.getSpoolContent(files.get(0)).split("\n");
    }

    private void printAll(String[] output, int size) {
        for (int i = 0; i < size; i++)
            terminal.println(output[i]);
    }

}
