package com.future;

import com.commands.Submit;
import com.dto.ResponseStatus;
import zowe.client.sdk.zosjobs.SubmitJobs;

import java.util.concurrent.Callable;

public class FutureSubmit extends Submit implements Callable<ResponseStatus> {

    private final String dataSet;
    private final String jobName;

    public FutureSubmit(SubmitJobs submitJobs, String dataSet, String jobName) {
        super(submitJobs);
        this.dataSet = dataSet;
        this.jobName = jobName;
    }

    @Override
    public ResponseStatus call() {
        return this.submitJob(dataSet, jobName);
    }

}
