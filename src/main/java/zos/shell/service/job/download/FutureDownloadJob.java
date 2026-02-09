package zos.shell.service.job.download;

import zos.shell.response.ResponseStatus;
import zos.shell.service.path.PathService;
import zos.shell.utility.FileUtil;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.Callable;

public class FutureDownloadJob extends DownloadJob implements Callable<ResponseStatus> {

    private final String target;
    private final String jobId;

    public FutureDownloadJob(final JobGet retrieve, final PathService pathService, final String target,
                             final boolean isAll, final String jobId, final long timeout) {
        super(retrieve, pathService, isAll, timeout);
        this.target = target;
        this.jobId = jobId;
    }

    @Override
    public ResponseStatus call() {
        ResponseStatus responseStatus = this.download(target, jobId);
        if (responseStatus != null && responseStatus.isStatus()) {
            FileUtil.openFileLocation(responseStatus.getOptionalData());
        }
        return responseStatus;
    }

}
