package zos.shell.service.job.browse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosjobs.methods.JobGet;

public class BrowseCmd extends BrowseLog {

    private static final Logger LOG = LoggerFactory.getLogger(BrowseCmd.class);

    public BrowseCmd(final JobGet retrieve, boolean isAll, final long timeout) {
        super(retrieve, isAll, timeout);
        LOG.debug("*** BrowseCmd ***");
    }

    public ResponseStatus browseJob(final String target) {
        LOG.debug("*** browseJob ***");
        return browseLog(target);
    }

}
