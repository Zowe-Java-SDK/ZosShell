package zos.shell.service.job.download;

import zos.shell.response.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.Callable;

public class FutureDownloadJob extends DownloadCmd implements Callable<ResponseStatus> {

    private final String name;

    public FutureDownloadJob(final JobGet retrieve, boolean isAll, final long timeout, final String name) {
        super(retrieve, isAll, timeout);
        this.name = name;
    }

    @Override
    public ResponseStatus call() {
        final var responseStatus = this.download(name);
        if (responseStatus != null && responseStatus.isStatus()) {
            Util.openFileLocation(responseStatus.getOptionalData());
        }
        return responseStatus;
    }

}
