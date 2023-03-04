package zos.shell.commands;

import zos.shell.Constants;
import zos.shell.dto.DataSetMember;
import zos.shell.dto.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.zosfiles.ZosDsnCopy;

public class Copy {

    private final ZosDsnCopy zosDsnCopy;

    public Copy(ZosDsnCopy zosDsnCopy) {
        this.zosDsnCopy = zosDsnCopy;
    }

    public ResponseStatus copy(String currDataSet, String[] params, boolean isSequential) {
        var fromDataSetName = "";
        var toDataSetName = "";
        var copyAllMembers = false;

        var param1 = params[1].toUpperCase();
        var param2 = params[2].toUpperCase();

        if (Util.isMember(param1)) {
            fromDataSetName = currDataSet + "(" + param1 + ")";
        }

        // if param1 does not contain period it means end user specified a member
        // if member check for member validity.
        if (!param1.contains(".")) {
            if (param1.charAt(param1.length() - 1) == '*') {
                final var member = param1.substring(0, param1.length() - 1);
                if (!Util.isMember(member)) {
                    return new ResponseStatus(Constants.INVALID_MEMBER, false);
                }
            }
        }

        if (Util.isMember(param2)) {
            toDataSetName = currDataSet + "(" + param2 + ")";
        }

        if (".".equals(param1) && ".".equals(param2)) {
            return new ResponseStatus(Constants.INVALID_COMMAND, false);
        }

        if (".".equals(param1)) {
            fromDataSetName = currDataSet;
            if (Util.isDataSet(param2)) {
                toDataSetName = param2;
            } else {
                return new ResponseStatus("second argument invalid for copy all operation, try again...", false);
            }
            copyAllMembers = true;
        }

        if (".".equals(param2)) {
            if (Util.isMember(param1)) {
                return new ResponseStatus(Constants.COPY_OPS_ITSELF_ERROR, false);
            }

            if (Util.isDataSet(param1)) {
                return new ResponseStatus(Constants.COPY_OPS_NO_MEMBER_ERROR, false);
            }

            if (param1.contains(currDataSet)) {
                return new ResponseStatus(Constants.COPY_OPS_ITSELF_ERROR, false);
            }

            if (param1.contains("(") && param1.contains(")")) {
                DataSetMember dataSetMember = Util.getMemberFromDataSet(param1);
                if (dataSetMember == null) {
                    return new ResponseStatus(Constants.COPY_OPS_NO_MEMBER_AND_DATASET_ERROR, false);
                }

                fromDataSetName = param1;
                toDataSetName = currDataSet + "(" + dataSetMember.getMember() + ")";
            }
        } else if (isSequential && Util.isDataSet(param1) && Util.isDataSet(param2)) {
            fromDataSetName = param1;
            toDataSetName = param2;
        } else if (isSequential && Util.isMember(param1) && Util.isDataSet(param2)) {
            fromDataSetName = currDataSet + "(" + param1 + ")";
            toDataSetName = param2;
        } else if (isSequential && Util.isDataSet(param1) && Util.isMember(param2)) {
            fromDataSetName = param1;
            toDataSetName = currDataSet + "(" + param2 + ")";
        } else {
            fromDataSetName = currDataSet + "(" + param1 + ")";
            toDataSetName = param2 + "(" + param1 + ")";
        }

        if (fromDataSetName.isEmpty()) {
            fromDataSetName = param1;
        }

        if (toDataSetName.isEmpty()) {
            toDataSetName = param2;
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

}
