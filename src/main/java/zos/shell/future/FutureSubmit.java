package zos.shell.future;

import zos.shell.commands.Submit;
import zos.shell.dto.ResponseStatus;
import zowe.client.sdk.zosjobs.methods.JobSubmit;

import java.util.concurrent.Callable;

public class FutureSubmit extends Submit implements Callable<ResponseStatus> {

    private final String dataSet;
    private final String jobName;

    public FutureSubmit(JobSubmit jobSubmit, String dataSet, String jobName) {
        super(jobSubmit);
        this.dataSet = dataSet;
        this.jobName = jobName;
    }

    @Override
    public ResponseStatus call() {
        return this.submitJob(dataSet, jobName);
    }

}
