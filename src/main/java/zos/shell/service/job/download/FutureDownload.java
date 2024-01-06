package zos.shell.service.job.download;

import zos.shell.response.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.Callable;

public class FutureDownload extends Download implements Callable<ResponseStatus> {

    private final String target;

    public FutureDownload(final JobGet retrieve, final String target, boolean isAll, final long timeout) {
        super(retrieve, isAll, timeout);
        this.target = target;
    }

    @Override
    public ResponseStatus call() {
        final var responseStatus = this.download(target);
        if (responseStatus != null && responseStatus.isStatus()) {
            Util.openFileLocation(responseStatus.getOptionalData());
        }
        return responseStatus;
    }

}
