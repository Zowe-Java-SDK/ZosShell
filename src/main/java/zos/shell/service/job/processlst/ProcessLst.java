package zos.shell.service.job.processlst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosjobs.input.GetJobParams;
import zowe.client.sdk.zosjobs.methods.JobGet;
import zowe.client.sdk.zosjobs.response.Job;

import java.util.Comparator;
import java.util.List;

public class ProcessLst {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessLst.class);

    private final JobGet jobGet;
    private final GetJobParams.Builder getJobParams = new GetJobParams.Builder("*");

    public ProcessLst(final JobGet jobGet) {
        LOG.debug("*** ProcessList ***");
        this.jobGet = jobGet;
    }

    public ResponseStatus processLst(final String jobOrTask) {
        LOG.debug("*** processLst ***");
        List<Job> jobs;
        try {
            if (jobOrTask != null) {
                getJobParams.prefix(jobOrTask).build();
            }
            final var params = getJobParams.build();
            jobs = jobGet.getCommon(params);
        } catch (ZosmfRequestException e) {
            final String errMsg = Util.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
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
