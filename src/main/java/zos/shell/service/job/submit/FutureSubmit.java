package zos.shell.service.job.submit;

import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosjobs.methods.JobSubmit;

import java.util.concurrent.Callable;

public class FutureSubmit extends Submit implements Callable<ResponseStatus> {

    private final String dataSet;
    private final String target;

    public FutureSubmit(final JobSubmit submit, final String dataSet, final String target) {
        super(submit);
        this.dataSet = dataSet;
        this.target = target;
    }

    @Override
    public ResponseStatus call() {
        return this.submit(dataSet, target);
    }

}
