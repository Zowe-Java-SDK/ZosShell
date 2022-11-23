package zos.shell.commands;

import zos.shell.dto.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.zosjobs.SubmitJobs;
import zowe.client.sdk.zosjobs.response.Job;

public class Submit {

    private final SubmitJobs submitJobs;

    public Submit(SubmitJobs submitJobs) {
        this.submitJobs = submitJobs;
    }

    public ResponseStatus submitJob(String dataSet, String param) {
        Job job;
        try {
            job = submitJobs.submitJob(String.format("%s(%s)", dataSet, param));
        } catch (Exception e) {
            return new ResponseStatus(Util.getErrorMsg(e + ""), false);
        }
        return new ResponseStatus("Job Name: " + job.getJobName().orElse("n\\a") +
                ", Job Id: " + job.getJobId().orElse("n\\a"), true);
    }

}
