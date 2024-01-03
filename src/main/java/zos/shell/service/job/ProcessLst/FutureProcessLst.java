package zos.shell.service.job.ProcessLst;

import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.Callable;

public class FutureProcessLst extends ProcessLstCmd implements Callable<ResponseStatus> {

    private final String jobOrTask;

    public FutureProcessLst(JobGet JobGet, String jobOrTask) {
        super(JobGet);
        this.jobOrTask = jobOrTask;
    }

    @Override
    public ResponseStatus call() {
        return this.ps(jobOrTask);
    }

}
