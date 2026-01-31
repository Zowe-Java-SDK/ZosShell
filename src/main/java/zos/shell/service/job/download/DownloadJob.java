package zos.shell.service.job.download;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.job.browse.BrowseLogService;
import zos.shell.service.path.PathService;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.FileUtil;
import zowe.client.sdk.zosjobs.methods.JobGet;
import zowe.client.sdk.zosjobs.model.Job;

import java.io.IOException;
import java.util.Comparator;

public class DownloadJob {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadJob.class);

    private final PathService pathService;
    private final BrowseLogService browseLogService;

    public DownloadJob(final JobGet retrieve, final PathService pathService, final boolean isAll, final long timeout) {
        LOG.debug("*** DownloadJob ***");
        this.pathService = pathService;
        this.browseLogService = new BrowseLogService(retrieve, isAll, timeout);
    }

    public ResponseStatus download(final String target) {
        LOG.debug("*** download ***");
        if (!DsnUtil.isMember(target)) {
            return new ResponseStatus(Constants.INVALID_MEMBER, false);
        }

        ResponseStatus responseStatus = browseLogService.browseJob(target);
        if (!responseStatus.isStatus() && responseStatus.getMessage().contains("timeout")) {
            return new ResponseStatus(Constants.TIMEOUT_MESSAGE, false);
        } else if (!responseStatus.isStatus()) {
            return new ResponseStatus(responseStatus.getMessage(), false);
        }

        var output = responseStatus.getMessage();
        String id = browseLogService.jobs.get(0).getJobId();

        this.pathService.createPathsForMember(target, id);
        try {
            FileUtil.writeTextFile(output, this.pathService.getPath(), this.pathService.getPathWithFile());
        } catch (IOException e) {
            return new ResponseStatus(e.getMessage(), false);
        }

        var message = Strings.padStart(target, 8, ' ') + Constants.ARROW +
                "downloaded to " + this.pathService.getPath();
        return new ResponseStatus(message, true, this.pathService.getPathWithFile());
    }

}
