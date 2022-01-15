package com.commands;

import org.beryx.textio.TextTerminal;
import zowe.client.sdk.zosjobs.GetJobs;

public class BrowseJob extends JobLog {

    public BrowseJob(TextTerminal<?> terminal, GetJobs getJobs, boolean isAll) {
        super(terminal, getJobs, isAll);
    }

    public StringBuilder browseJob(String param) throws Exception {
        try {
            return browseJobLog(param);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

}
