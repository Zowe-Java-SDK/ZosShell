package com.command;

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
    private final ZOSConnection connection;
    private GetJobParams.Builder jobParams = new GetJobParams.Builder("*");
    private GetJobs getJobs;
    private final int LINES_LIMIT = 25;

    public GetJobOutput(TextTerminal<?> terminal, ZOSConnection connection) {
        this.terminal = terminal;
        this.connection = connection;
        this.getJobs = new GetJobs(connection);
    }

    public String[] getLog(String param) {
        String[] output;
        try {
            output = getJobLog(getJobs, jobParams.prefix(param).build());
        } catch (Exception e) {
            if (e.getMessage().contains("Connection refused")) {
                terminal.printf(Constants.SEVERE_ERROR + "\n");
                return null;
            }
            Util.printError(terminal, e.getMessage());
            return null;
        }
        return output;
    }

    public void tail(String[] params) {
        String[] output = getLog(params[1]);
        if (output == null) return;
        int size = output.length;
        int lines = 0;
        if (params.length == 3) {
            try {
                lines = Integer.parseInt(params[2]);
            } catch (NumberFormatException e) {
                terminal.printf(Constants.INVALID_PARAMETER + "\n");
                return;
            }
        }

        if (lines > 0) {
            if (lines < size) {
                for (int i = size - lines; i < size; i++)
                    terminal.printf(output[i] + "\n");
            } else {
                printAll(output, size);
            }
        } else {
            if (size > LINES_LIMIT) {
                for (int i = size - LINES_LIMIT; i < size; i++)
                    terminal.printf(output[i] + "\n");
            } else {
                printAll(output, size);
            }
        }
    }

    private String[] getJobLog(GetJobs getJobs, GetJobParams jobParams) throws Exception {
        final var jobs = getJobs.getJobsCommon(jobParams);
        // select the active one first not found then get the highest job number
        Optional<Job> job = jobs.stream().filter(j -> "ACTIVE".equalsIgnoreCase(j.getStatus().get())).findAny();
        final var files = getJobs.getSpoolFilesForJob(job.orElse(jobs.get(0)));
        return getJobs.getSpoolContent(files.get(0)).split("\n");
    }

    private void printAll(String[] output, int size) {
        for (int i = 0; i < size; i++)
            terminal.printf(output[i] + "\n");
    }

}
