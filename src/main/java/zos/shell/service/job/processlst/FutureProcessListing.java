package zos.shell.service.job.processlst;

import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.Callable;

public class FutureProcessListing extends ProcessListing implements Callable<ResponseStatus> {

    private final String jobOrTask;

    public FutureProcessListing(final JobGet JobGet, final String jobOrTask) {
        super(JobGet);
        this.jobOrTask = jobOrTask;
    }

    @Override
    public ResponseStatus call() {
        return this.processLst(jobOrTask);
    }

}
