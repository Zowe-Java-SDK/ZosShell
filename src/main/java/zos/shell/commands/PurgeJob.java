package zos.shell.commands;

import zos.shell.dto.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.core.ZOSConnection;
import zowe.client.sdk.rest.Response;
import zowe.client.sdk.zosjobs.DeleteJobs;
import zowe.client.sdk.zosjobs.GetJobs;
import zowe.client.sdk.zosjobs.input.GetJobParams;
import zowe.client.sdk.zosjobs.input.ModifyJobParams;
import zowe.client.sdk.zosjobs.response.Job;

import java.util.Comparator;
import java.util.List;

public class PurgeJob {

    private DeleteJobs deleteJobs;
    private GetJobs getJobs;

    private final GetJobParams.Builder getJobParams = new GetJobParams.Builder("*");

    public PurgeJob(ZOSConnection connection) {
        deleteJobs = new DeleteJobs(connection);
        getJobs = new GetJobs(connection);
    }

    public ResponseStatus purgeJobByJobName(String jobName) {
        List<Job> jobs;
        try {
            jobs = getJobs.getJobsByPrefix(jobName);
        } catch (Exception e) {
            return new ResponseStatus(Util.getErrorMsg(e + ""), false);
        }

        if (jobs.isEmpty()) {
            return new ResponseStatus("job not found", false);
        }
        jobs.sort(Comparator.comparing(job -> job.getJobId().get(), Comparator.reverseOrder()));
        final var job = jobs.get(0);
        return purgeJob(job);
    }

    public ResponseStatus purgeJobByJobId(String jobId) {
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
        if (!job.getJobId().isPresent()) {
            return new ResponseStatus("job id not found", false);
        }
        if (!job.getJobName().isPresent()) {
            return new ResponseStatus("job name not found", false);
        }

        ModifyJobParams modifyJobParams = new ModifyJobParams.Builder(
                job.getJobName().get(), job.getJobId().get()).version("1.0").build();
        try {
            Response response = deleteJobs.deleteJobCommon(modifyJobParams);
            final var code = response.getStatusCode().orElse(-1);
            if (!Util.isHttpError(code)) {
                return new ResponseStatus("Job Name: " + job.getJobName().get() +
                        ", Job Id: " + job.getJobId().get() + " purged successfully...", true);
            } else {
                return new ResponseStatus("Job Name: " + job.getJobName().get() + ", Job Id: " +
                        job.getJobId().get() + " purge failed with http code " + code + ", try again...", true);
            }
        } catch (Exception e) {
            return new ResponseStatus(Util.getErrorMsg(e + ""), false);
        }
    }

}
