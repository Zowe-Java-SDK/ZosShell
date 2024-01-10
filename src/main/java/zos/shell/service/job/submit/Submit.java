package zos.shell.service.job.submit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosjobs.methods.JobSubmit;
import zowe.client.sdk.zosjobs.response.Job;

public class Submit {

    private static final Logger LOG = LoggerFactory.getLogger(Submit.class);

    private final JobSubmit submit;

    public Submit(final JobSubmit submit) {
        LOG.debug("*** Submit ***");
        this.submit = submit;
    }

    public ResponseStatus submit(final String dataset, final String target) {
        LOG.debug("*** submit ***");
        Job job;
        try {
            job = submit.submit(String.format("%s(%s)", dataset, target));
        } catch (ZosmfRequestException e) {
            final String errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }
        return new ResponseStatus("Job Name: " + job.getJobName().orElse("n\\a") +
                ", Job Id: " + job.getJobId().orElse("n\\a"), true);
    }

}
