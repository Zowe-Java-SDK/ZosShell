package zos.shell.future;

import zos.shell.service.job.DownloadCmd;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.Callable;

public class FutureDownloadJob extends DownloadCmd implements Callable<ResponseStatus> {

    private final String jobName;

    public FutureDownloadJob(JobGet JobGet, boolean isAll, long timeout, String jobName) {
        super(JobGet, isAll, timeout);
        this.jobName = jobName;
    }

    @Override
    public ResponseStatus call() {
        final var responseStatus = this.download(jobName);
        if (responseStatus != null && responseStatus.isStatus()) {
            Util.openFileLocation(responseStatus.getOptionalData());
        }
        return responseStatus;
    }

}
