package zos.shell.service.job.purge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.ResponseUtil;
import zos.shell.utility.Util;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosjobs.input.ModifyJobParams;
import zowe.client.sdk.zosjobs.methods.JobDelete;
import zowe.client.sdk.zosjobs.methods.JobGet;
import zowe.client.sdk.zosjobs.response.Job;

import java.util.Comparator;
import java.util.List;

public class Purge {

    private static final Logger LOG = LoggerFactory.getLogger(Purge.class);

    private final JobDelete delete;
    private final JobGet retrieve;

    public Purge(final JobDelete delete, final JobGet retrieve) {
        LOG.debug("*** Purge ***");
        this.delete = delete;
        this.retrieve = retrieve;
    }

    public ResponseStatus purgeByName(final String name) {
        LOG.debug("*** purgeByName ***");
        if (!Util.isMember(name)) {
            return new ResponseStatus("invalid name or id specified, try again...", false);
        }

        List<Job> jobs;
        try {
            jobs = retrieve.getByPrefix(name);
        } catch (ZosmfRequestException e) {
            final String errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }

        if (jobs.isEmpty()) {
            return new ResponseStatus("job not found", false);
        }

        jobs.sort(Comparator.comparing(job -> job.getJobId().orElse(""), Comparator.reverseOrder()));
        return purge(jobs.get(0));
    }

    public ResponseStatus purgeById(final String id) {
        LOG.debug("*** purgeById ***");
        if (!Util.isMember(id)) {
            return new ResponseStatus("invalid name or id specified, try again...", false);
        }

        Job job;
        try {
            job = retrieve.getById(id);
        } catch (ZosmfRequestException e) {
            final String errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }

        if (job == null) {
            return new ResponseStatus("job not found", false);
        }

        return purge(job);
    }

    private ResponseStatus purge(final Job job) {
        LOG.debug("*** purge ***");

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
            // TODO check response value
            delete.deleteCommon(new ModifyJobParams.Builder(
                    job.getJobName().get(), job.getJobId().get()).version("1.0").build());
            final var msg = "Job Name: " + job.getJobName().get() + ", Job Id: " + job.getJobId().get() +
                    " purged successfully...";
            return new ResponseStatus(msg, true);
        } catch (ZosmfRequestException e) {
            final String errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }
    }

}
