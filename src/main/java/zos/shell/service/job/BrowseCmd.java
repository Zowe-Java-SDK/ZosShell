package zos.shell.service.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosjobs.methods.JobGet;

public class BrowseCmd extends BrowseLog {

    private static final Logger LOG = LoggerFactory.getLogger(BrowseCmd.class);

    public BrowseCmd(JobGet jobGet, boolean isAll, long timeout) {
        super(jobGet, isAll, timeout);
        LOG.debug("*** BrowseJob ***");
    }

    public ResponseStatus browseJob(String param) {
        LOG.debug("*** browseJob ***");
        return browseJobLog(param);
    }

}
