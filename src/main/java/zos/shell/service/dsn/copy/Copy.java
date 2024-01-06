package zos.shell.service.dsn.copy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.record.DataSetMember;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.ResponseUtil;
import zos.shell.utility.Util;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCopy;

public class Copy {

    private static final Logger LOG = LoggerFactory.getLogger(Copy.class);

    private final DsnCopy dsnCopy;

    public Copy(final DsnCopy dsnCopy) {
        LOG.debug("*** Copy ***");
        this.dsnCopy = dsnCopy;
    }

    public ResponseStatus copy(final String currDataSet, final String[] params) {
        LOG.debug("*** copy ***");
        var fromDataSetName = "";
        var toDataSetName = "";

        final var firstParam = params[1].toUpperCase();
        final var secondParam = params[2].toUpperCase();

        if (".".equals(firstParam) && ".".equals(secondParam)) {
            return new ResponseStatus(Constants.INVALID_ARGUMENTS, false);
        }

        final var datasetMemberFirstParam = DataSetMember.getDatasetAndMember(firstParam);
        if (datasetMemberFirstParam != null) {
            fromDataSetName = datasetMemberFirstParam.getDataSet() + "(" + datasetMemberFirstParam.getMember() + ")";
        }

        final var datasetMemberSecondParam = DataSetMember.getDatasetAndMember(secondParam);
        if (datasetMemberSecondParam != null) {
            toDataSetName = datasetMemberSecondParam.getDataSet() + "(" + datasetMemberSecondParam.getMember() + ")";
        }

        // copy dataset(member) to dataset(member)
        if (datasetMemberFirstParam != null && datasetMemberSecondParam != null) {
            return doCopy(fromDataSetName, toDataSetName, false);
        }

        // copy dataset to dataset
        if (Util.isDataSet(firstParam) && Util.isDataSet(secondParam)) {
            return doCopy(firstParam, secondParam, false);
        }

        if (Util.isMember(firstParam)) {
            if (currDataSet.isBlank()) {
                return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false);
            }
            fromDataSetName = currDataSet + "(" + firstParam + ")";
        }

        if (Util.isMember(secondParam)) {
            if (currDataSet.isBlank()) {
                return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false);
            }
            toDataSetName = currDataSet + "(" + secondParam + ")";
        }

        // copy currDataSet(member) to currDataSet(member)
        if (!fromDataSetName.isBlank() && !toDataSetName.isBlank()) {
            return doCopy(fromDataSetName, toDataSetName, false);
        }

        // copy ./* to dataset
        if (".".equals(firstParam) || "*".equals(firstParam)) {
            if (currDataSet.isBlank()) {
                return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false);
            }
            fromDataSetName = currDataSet;
            if (Util.isDataSet(secondParam)) {
                toDataSetName = secondParam;
            } else {
                return new ResponseStatus("specify valid dataset destination, try again...", false);
            }
            return doCopy(fromDataSetName, toDataSetName, true);
        }

        // copy currDataSet(member) to .
        // copy dataset(member) to .
        if (".".equals(secondParam)) {
            if (currDataSet.isBlank()) {
                return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false);
            }
            if (datasetMemberFirstParam != null) {
                return doCopy(fromDataSetName, currDataSet, true);
            }
            if (Util.isMember(firstParam)) {
                return doCopy(currDataSet + "(" + firstParam + ")", currDataSet, true);
            }
            if (Util.isDataSet(firstParam)) {
                return new ResponseStatus(Constants.COPY_NO_MEMBER_ERROR, false);
            }
        }

        // copy dataset(member) to currDataSet(member)
        if (datasetMemberFirstParam != null && Util.isMember(secondParam)) {
            return doCopy(fromDataSetName, currDataSet + "(" + secondParam + ")", false);
        }

        return new ResponseStatus(Constants.INVALID_ARGUMENTS, false);
    }

    private ResponseStatus doCopy(final String target, final String destination, boolean isCopyAll) {
        LOG.debug("*** doCopy ***");
        try {
            dsnCopy.copy(target, destination, true, isCopyAll);
        } catch (ZosmfRequestException e) {
            final String errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }
        return new ResponseStatus("copied to " + destination, true);
    }

}
