package zos.shell.service.job.processlst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosjobs.input.JobGetInputData;
import zowe.client.sdk.zosjobs.methods.JobGet;
import zowe.client.sdk.zosjobs.model.Job;

import java.util.Comparator;
import java.util.List;

public class ProcessListing {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessListing.class);

    private final JobGet jobGet;
    private final JobGetInputData.Builder getJobParams = new JobGetInputData.Builder("*");

    public ProcessListing(final JobGet jobGet) {
        LOG.debug("*** ProcessListing ***");
        this.jobGet = jobGet;
    }

    public ResponseStatus processLst(final String target) {
        LOG.debug("*** processLst ***");
        List<Job> jobs;
        try {
            if (target != null) {
                getJobParams.prefix(target).build();
            }
            var params = getJobParams.build();
            jobs = jobGet.getCommon(params);
        } catch (ZosmfRequestException e) {
            final String errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }
        if (jobs.isEmpty()) {
            return new ResponseStatus(Constants.NO_PROCESS_FOUND, false);
        }
        var str = new StringBuilder();
        jobs.forEach(job -> {
            var jobName = job.getJobName();
            var jobId = job.getJobId();
            var jobStatus = job.getStatus();
            str.append(String.format("%-8s %-8s %-8s", jobName, jobId, jobStatus));
            str.append("\n");
        });
        return new ResponseStatus(str.toString(), true);
    }

}
