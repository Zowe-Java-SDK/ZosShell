package com.commands;

import com.Constants;
import com.dto.ResponseStatus;
import com.utility.Util;
import zowe.client.sdk.zosjobs.GetJobs;
import zowe.client.sdk.zosjobs.input.GetJobParams;
import zowe.client.sdk.zosjobs.response.Job;

import java.util.Comparator;
import java.util.List;

public class ProcessList {

    private final GetJobs getJobs;
    private final GetJobParams.Builder getJobParams = new GetJobParams.Builder("*");

    public ProcessList(GetJobs getJobs) {
        this.getJobs = getJobs;
    }

    public ResponseStatus ps(String jobOrTask) {
        List<Job> jobs;
        try {
            if (jobOrTask != null) {
                getJobParams.prefix(jobOrTask).build();
            }
            final var params = getJobParams.build();
            jobs = getJobs.getJobsCommon(params);
        } catch (Exception e) {
            return new ResponseStatus(Util.getErrorMsg(e + ""), false);
        }
        jobs.sort(Comparator.comparing((Job j) -> j.getJobName().orElse(""))
                .thenComparing(j -> j.getStatus().orElse(""))
                .thenComparing(j -> j.getJobId().orElse("")));
        if (jobs.isEmpty()) {
            return new ResponseStatus(Constants.NO_PROCESS_FOUND, false);
        }
        final var str = new StringBuilder();
        jobs.forEach(job -> {
            final var jobName = job.getJobName().orElse("");
            final var jobId = job.getJobId().orElse("");
            final var jobStatus = job.getStatus().orElse("");
            str.append(String.format("%-8s %-8s %-8s", jobName, jobId, jobStatus));
            str.append("\n");
        });
        return new ResponseStatus(str.toString(), true);
    }

}
