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

import java.io.IOException;
import java.util.Comparator;

public class DownloadJob {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadJob.class);

    private final BrowseLogService browseLogService;

    public DownloadJob(final JobGet retrieve, boolean isAll, final long timeout) {
        LOG.debug("*** DownloadJob ***");
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
        browseLogService.jobs.sort(Comparator.comparing(job -> job.getJobId().orElse(""), Comparator.reverseOrder()));
        String id = browseLogService.jobs.get(0).getJobId().orElse(null);

        var pathService = new PathService(target, id);
        try {
            FileUtil.writeTextFile(output, pathService.getPath(), pathService.getPathWithFile());
        } catch (IOException e) {
            return new ResponseStatus(e.getMessage(), false);
        }

        var message = Strings.padStart(target, 8, ' ') + Constants.ARROW +
                "downloaded to " + pathService.getPath();
        return new ResponseStatus(message, true, pathService.getPathWithFile());
    }

}
