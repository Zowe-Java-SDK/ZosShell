package com.commands;

import org.beryx.textio.TextTerminal;
import zosjobs.GetJobs;

import java.util.List;

public class GetJobLog extends JobLog {

    public GetJobLog(TextTerminal<?> terminal, GetJobs getJobs, boolean isAll) {
        super(terminal, getJobs, isAll);
    }

    public List<String> getLog(String param) throws Exception {
        try {
            return getJobLog(param);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

}
