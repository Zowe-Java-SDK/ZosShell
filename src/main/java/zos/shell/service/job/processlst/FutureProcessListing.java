package zos.shell.service.job.processlst;

import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.Callable;

public class FutureProcessListing extends ProcessListing implements Callable<ResponseStatus> {

    private final String target;

    public FutureProcessListing(final JobGet jobGet, final String target) {
        super(jobGet);
        this.target = target;
    }

    @Override
    public ResponseStatus call() {
        return this.processLst(target);
    }

}
