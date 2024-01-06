package zos.shell.service.job.download;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.job.browse.BrowseCmd;
import zos.shell.utility.DirectorySetup;
import zos.shell.utility.Util;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.io.IOException;
import java.util.Comparator;

public class Download {

    private static final Logger LOG = LoggerFactory.getLogger(Download.class);

    private final BrowseCmd browseCmd;

    public Download(final JobGet retrieve, boolean isAll, final long timeout) {
        LOG.debug("*** Download ***");
        this.browseCmd = new BrowseCmd(retrieve, isAll, timeout);
    }

    public ResponseStatus download(final String target) {
        LOG.debug("*** download ***");
        if (!Util.isMember(target)) {
            return new ResponseStatus(Constants.INVALID_MEMBER, false);
        }

        final var responseStatus = browseCmd.browseJob(target);
        if (!responseStatus.isStatus() && responseStatus.getMessage().contains("timeout")) {
            return new ResponseStatus(Constants.TIMEOUT_MESSAGE, false);
        } else if (!responseStatus.isStatus()) {
            return new ResponseStatus(responseStatus.getMessage(), false);
        }

        final var output = responseStatus.getMessage();
        browseCmd.jobs.sort(Comparator.comparing(job -> job.getJobId().orElse(""), Comparator.reverseOrder()));
        final var id = browseCmd.jobs.get(0).getJobId().orElse(null);

        final var dirSetup = new DirectorySetup();
        try {
            dirSetup.initialize(target, id);
        } catch (IllegalStateException e) {
            return new ResponseStatus(e.getMessage(), false);
        }

        try {
            Util.writeTextFile(output, dirSetup.getDirectoryPath(), dirSetup.getFileNamePath());
        } catch (IOException e) {
            return new ResponseStatus(e.getMessage(), false);
        }

        final var message = Strings.padStart(target, 8, ' ') + Constants.ARROW +
                "downloaded to " + dirSetup.getFileNamePath();
        return new ResponseStatus(message, true, dirSetup.getFileNamePath());
    }

}
