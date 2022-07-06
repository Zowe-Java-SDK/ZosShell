package com.future;

import com.commands.ProcessList;
import com.dto.ResponseStatus;
import zowe.client.sdk.zosjobs.GetJobs;

import java.util.concurrent.Callable;

public class FutureProcessList extends ProcessList implements Callable<ResponseStatus> {

    private final String jobOrTask;

    public FutureProcessList(GetJobs getJobs, String jobOrTask) {
        super(getJobs);
        this.jobOrTask = jobOrTask;
    }

    @Override
    public ResponseStatus call() {
        return this.ps(jobOrTask);
    }

}
