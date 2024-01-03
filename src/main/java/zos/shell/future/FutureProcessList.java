package zos.shell.future;

import zos.shell.response.ResponseStatus;
import zos.shell.service.job.LstCmd;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.Callable;

public class FutureProcessList extends LstCmd implements Callable<ResponseStatus> {

    private final String jobOrTask;

    public FutureProcessList(JobGet JobGet, String jobOrTask) {
        super(JobGet);
        this.jobOrTask = jobOrTask;
    }

    @Override
    public ResponseStatus call() {
        return this.ps(jobOrTask);
    }

}
