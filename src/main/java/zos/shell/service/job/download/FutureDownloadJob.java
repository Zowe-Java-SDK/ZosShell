package zos.shell.service.job.download;

import zos.shell.response.ResponseStatus;
import zos.shell.utility.FileUtil;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.Callable;

public class FutureDownloadJob extends DownloadJob implements Callable<ResponseStatus> {

    private final String target;

    public FutureDownloadJob(final JobGet retrieve, final String target, boolean isAll, final long timeout) {
        super(retrieve, isAll, timeout);
        this.target = target;
    }

    @Override
    public ResponseStatus call() {
        ResponseStatus responseStatus = this.download(target);
        if (responseStatus != null && responseStatus.isStatus()) {
            FileUtil.openFileLocation(responseStatus.getOptionalData());
        }
        return responseStatus;
    }

}
