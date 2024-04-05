package zos.shell.service.job.submit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.record.DatasetMember;
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

    public ResponseStatus submit(String dataset, String target) {
        LOG.debug("*** submit ***");
        var datasetMember = DatasetMember.getDatasetAndMember(target);
        if (datasetMember != null) {
            // dataset(member) input specified
            dataset = datasetMember.getDataset();
            target = datasetMember.getMember();
        }

        if (dataset.isBlank()) {
            return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false);
        }

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
