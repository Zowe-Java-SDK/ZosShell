package zos.shell.future;

import zos.shell.commands.ProcessList;
import zos.shell.dto.ResponseStatus;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.Callable;

public class FutureProcessList extends ProcessList implements Callable<ResponseStatus> {

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
