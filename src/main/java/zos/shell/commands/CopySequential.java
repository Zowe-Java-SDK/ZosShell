package zos.shell.commands;

import zos.shell.Constants;
import zos.shell.dto.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.zosfiles.ZosDsnCopy;

public class CopySequential {

    private final ZosDsnCopy zosDsnCopy;

    public CopySequential(ZosDsnCopy zosDsnCopy) {
        this.zosDsnCopy = zosDsnCopy;
    }

    public ResponseStatus copy(String currDataSet, String[] params) {
        var fromDataSetName = "";
        var toDataSetName = "";

        var param1 = params[1].toUpperCase();
        var param2 = params[2].toUpperCase();

        if (Util.isMember(param1) && Util.isMember(param2)) {
            return new ResponseStatus("invalid arguments, specify at least one valid sequential dataset, try again...", false);
        }

        if (Util.isMember(param1) && !Util.isDataSet(param2)) {
            return new ResponseStatus("invalid second argument, specify a valid sequential dataset, try again...", false);
        }

        if (!Util.isMember(param1) && Util.isDataSet(param2)) {
            return new ResponseStatus("invalid first argument, specify a valid member or sequential dataset, try again...", false);
        }

        if (Util.isMember(param2) && !Util.isDataSet(param1)) {
            return new ResponseStatus("invalid first argument, specify a valid sequential dataset, try again...", false);
        }

        if (!Util.isMember(param2) && Util.isDataSet(param1)) {
            return new ResponseStatus("invalid second argument, specify a valid member or sequential dataset, try again...", false);
        }

        if ((!Util.isMember(param1) && !Util.isMember(param2)) && (!Util.isDataSet(param1) && !Util.isDataSet(param2))) {
            return new ResponseStatus("invalid arguments, try again...", false);
        }

        if (Util.isDataSet(param1) && Util.isDataSet(param2)) {
            fromDataSetName = param1;
            toDataSetName = param2;
        } else if (Util.isMember(param1) && Util.isDataSet(param2)) {
            fromDataSetName = currDataSet + "(" + param1 + ")";
            toDataSetName = param2;
        } else if (Util.isDataSet(param1) && Util.isMember(param2)) {
            fromDataSetName = param1;
            toDataSetName = currDataSet + "(" + param2 + ")";
        }

        try {
            zosDsnCopy.copy(fromDataSetName, toDataSetName, true, false);
        } catch (Exception e) {
            if (e.getMessage().contains(Constants.CONNECTION_REFUSED)) {
                return new ResponseStatus(Constants.SEVERE_ERROR, false);
            }
            return new ResponseStatus(e.getMessage(), false);
        }
        return new ResponseStatus("copied to " + toDataSetName, true);
    }

}

