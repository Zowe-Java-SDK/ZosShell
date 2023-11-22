package zos.shell.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.dto.ResponseStatus;
import zowe.client.sdk.zosjobs.methods.JobGet;

public class BrowseJob extends JobLog {

    private static final Logger LOG = LoggerFactory.getLogger(BrowseJob.class);

    public BrowseJob(JobGet jobGet, boolean isAll, long timeout) {
        super(jobGet, isAll, timeout);
        LOG.debug("*** BrowseJob ***");
    }

    public ResponseStatus browseJob(String param) {
        LOG.debug("*** browseJob ***");
        return browseJobLog(param);
    }

}
