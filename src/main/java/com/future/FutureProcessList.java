package com.future;

import com.commands.ProcessList;
import com.dto.ResponseStatus;
import org.beryx.textio.TextTerminal;
import zowe.client.sdk.zosjobs.GetJobs;

import java.util.concurrent.Callable;

public class FutureProcessList extends ProcessList implements Callable<ResponseStatus> {

    private final String jobOrTask;

    public FutureProcessList(TextTerminal<?> terminal, GetJobs getJobs, String jobOrTask) {
        super(terminal, getJobs);
        this.jobOrTask = jobOrTask;
    }

    @Override
    public ResponseStatus call() {
        return this.ps(jobOrTask);
    }

}
