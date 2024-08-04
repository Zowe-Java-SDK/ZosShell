package zos.shell.service.job.browse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosjobs.methods.JobGet;

public class BrowseLogService extends BrowseLog {

    private static final Logger LOG = LoggerFactory.getLogger(BrowseLogService.class);

    public BrowseLogService(final JobGet retrieve, final boolean isAll, final long timeout) {
        super(retrieve, isAll, timeout);
        LOG.debug("*** BrowseLogService ***");
    }

    public ResponseStatus browseJob(final String target) {
        LOG.debug("*** browseJob ***");
        return browseLog(target);
    }

}
