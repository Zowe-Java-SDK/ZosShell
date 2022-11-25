package zos.shell.commands;

import zos.shell.dto.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.core.ZOSConnection;
import zowe.client.sdk.zosjobs.DeleteJobs;
import zowe.client.sdk.zosjobs.GetJobs;
import zowe.client.sdk.zosjobs.input.ModifyJobParams;
import zowe.client.sdk.zosjobs.response.Job;

import java.util.Comparator;
import java.util.List;

public class PurgeJob {

    private final DeleteJobs deleteJobs;
    private final GetJobs getJobs;

    public PurgeJob(ZOSConnection connection) {
        deleteJobs = new DeleteJobs(connection);
        getJobs = new GetJobs(connection);
    }

    public ResponseStatus purgeJobByJobName(String jobName) {
        if (!Util.isMember(jobName)) {
            return new ResponseStatus("invalid name or id specified, try again...", false);
        }

        List<Job> jobs;
        try {
            jobs = getJobs.getJobsByPrefix(jobName);
        } catch (Exception e) {
            return new ResponseStatus(Util.getErrorMsg(e + ""), false);
        }

        if (jobs.isEmpty()) {
            return new ResponseStatus("job not found", false);
        }
        jobs.sort(Comparator.comparing(job -> job.getJobId().orElse(""), Comparator.reverseOrder()));
        return purgeJob(jobs.get(0));
    }

    public ResponseStatus purgeJobByJobId(String jobId) {
        if (!Util.isMember(jobId)) {
            return new ResponseStatus("invalid name or id specified, try again...", false);
        }
        Job job;
        try {
            job = getJobs.getJob(jobId);
        } catch (Exception e) {
            return new ResponseStatus(Util.getErrorMsg(e + ""), false);
        }

        if (job == null) {
            return new ResponseStatus("job not found", false);
        }
        return purgeJob(job);
    }

    private ResponseStatus purgeJob(Job job) {
        if (job.getJobId().isEmpty()) {
            return new ResponseStatus("job id not found", false);
        }
        if (job.getJobName().isEmpty()) {
            return new ResponseStatus("job name not found", false);
        }
        if (job.getStatus().isEmpty()) {
            return new ResponseStatus("job status not found", false);
        }
        if (!"OUTPUT".equals(job.getStatus().get())) {
            return new ResponseStatus("cannot purge active job", false);
        }

        try {
            final var modifyJobParams = new ModifyJobParams.Builder(
                    job.getJobName().get(), job.getJobId().get()).version("1.0").build();
            deleteJobs.deleteJobCommon(modifyJobParams);
            return new ResponseStatus("Job Name: " + job.getJobName().get() +
                    ", Job Id: " + job.getJobId().get() + " purged successfully...", true);
        } catch (Exception e) {
            return new ResponseStatus(Util.getErrorMsg(e + ""), false);
        }
    }

}
