package zos.shell.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.Constants;
import zos.shell.dto.DataSetMember;
import zos.shell.dto.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.zosfiles.ZosDsnCopy;

public class Copy {

    private static final Logger LOG = LoggerFactory.getLogger(Copy.class);

    private final ZosDsnCopy zosDsnCopy;

    public Copy(ZosDsnCopy zosDsnCopy) {
        LOG.debug("*** Copy ***");
        this.zosDsnCopy = zosDsnCopy;
    }

    public ResponseStatus copy(String currDataSet, String[] params) {
        LOG.debug("*** copy ***");
        var fromDataSetName = "";
        var toDataSetName = "";
        var copyAllMembers = false;

        final var firstParam = params[1].toUpperCase();
        final var secondParam = params[2].toUpperCase();

        final var datasetMemberFirstParam = Util.getDatasetAndMember(firstParam);
        if (datasetMemberFirstParam != null) {
            fromDataSetName = datasetMemberFirstParam.getDataSet() + "(" + datasetMemberFirstParam.getMember() + ")";
        }

        final var datasetMemberSecondParam = Util.getDatasetAndMember(secondParam);
        if (datasetMemberSecondParam != null) {
            toDataSetName = datasetMemberSecondParam.getDataSet() + "(" + datasetMemberSecondParam.getMember() + ")";
        }

        if (!(Util.isMember(firstParam) || ".".equals(firstParam) || datasetMemberFirstParam != null)) {
            return new ResponseStatus("specify valid member or dataset(member) value for first argument, try again...", false);
        }
        if (!(Util.isDataSet(secondParam) || Util.isMember(secondParam) || ".".equals(secondParam) ||
                datasetMemberSecondParam != null)) {
            return new ResponseStatus("specify valid member or dataset(member) value for second argument, try again...", false);
        }
        if (".".equals(firstParam) && ".".equals(secondParam)) {
            return new ResponseStatus(Constants.INVALID_ARGUMENTS, false);
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
            copyAllMembers = true;
        }

        if (isEqualDatasets(currDataSet, firstParam, datasetMemberFirstParam)) {
            return new ResponseStatus(Constants.COPY_ITSELF_ERROR, false);
        }

        if (isEqualDatasets(currDataSet, secondParam, datasetMemberSecondParam)) {
            return new ResponseStatus(Constants.COPY_ITSELF_ERROR, false);
        }

        if (".".equals(secondParam)) {
            if (Util.isMember(firstParam)) {
                return new ResponseStatus(Constants.COPY_ITSELF_ERROR, false);
            }

            if (Util.isDataSet(firstParam)) {
                return new ResponseStatus(Constants.COPY_NO_MEMBER_ERROR, false);
            }

            if (firstParam.contains("(") && firstParam.contains(")")) {
                DataSetMember dataSetMember = Util.getDatasetAndMember(firstParam);
                if (dataSetMember == null) {
                    return new ResponseStatus(Constants.COPY_NO_MEMBER_AND_DATASET_ERROR, false);
                }

                fromDataSetName = firstParam;
                toDataSetName = currDataSet + "(" + dataSetMember.getMember() + ")";
            }
        }

        if (Util.isMember(firstParam) && Util.isDataSet(secondParam)) {
            fromDataSetName = currDataSet + "(" + firstParam + ")";
            toDataSetName = secondParam + "(" + firstParam + ")";
        }

        if (fromDataSetName.isEmpty()) {
            fromDataSetName = firstParam;
        }

        if (toDataSetName.isEmpty()) {
            toDataSetName = secondParam;
        }
        try {
            zosDsnCopy.copy(fromDataSetName, toDataSetName, true, copyAllMembers);
        } catch (Exception e) {
            if (e.getMessage().contains(Constants.CONNECTION_REFUSED)) {
                return new ResponseStatus(Constants.SEVERE_ERROR, false);
            }
            return new ResponseStatus(e.getMessage(), false);
        }
        return new ResponseStatus("copied to " + toDataSetName, true);
    }

    private boolean isEqualDatasets(final String currDataSet, final String firstParam,
                                   final DataSetMember dataSetMemberFirstParam) {
        if (firstParam.contains(currDataSet)) {
            int size = firstParam.length();
            if (dataSetMemberFirstParam != null) {
                size = dataSetMemberFirstParam.getDataSet().length();
            }
            return size == currDataSet.length();
        }
        return false;
    }

}
