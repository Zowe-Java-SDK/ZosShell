package zos.shell.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.dto.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosjobs.methods.JobSubmit;
import zowe.client.sdk.zosjobs.response.Job;

public class Submit {

    private static final Logger LOG = LoggerFactory.getLogger(Submit.class);

    private final JobSubmit jobSubmit;

    public Submit(JobSubmit jobSubmit) {
        LOG.debug("*** Submit ***");
        this.jobSubmit = jobSubmit;
    }

    public ResponseStatus submitJob(String dataSet, String param) {
        LOG.debug("*** submitJob ***");
        Job job;
        try {
            job = jobSubmit.submit(String.format("%s(%s)", dataSet, param));
        } catch (ZosmfRequestException e) {
            final String errMsg = Util.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }
        return new ResponseStatus("Job Name: " + job.getJobName().orElse("n\\a") +
                ", Job Id: " + job.getJobId().orElse("n\\a"), true);
    }

}
