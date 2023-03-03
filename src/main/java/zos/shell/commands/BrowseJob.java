package zos.shell.commands;

import zos.shell.dto.ResponseStatus;
import zowe.client.sdk.zosjobs.GetJobs;

public class BrowseJob extends JobLog {

    public BrowseJob(GetJobs getJobs, boolean isAll, long timeout) {
        super(getJobs, isAll, timeout);
    }

    public ResponseStatus browseJob(String param) {
        return browseJobLog(param);
    }

}
