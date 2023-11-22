package zos.shell.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.Constants;
import zos.shell.dto.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCopy;

public class CopySequential {

    private static final Logger LOG = LoggerFactory.getLogger(CopySequential.class);

    private final DsnCopy DsnCopy;

    public CopySequential(DsnCopy DsnCopy) {
        LOG.debug("*** CopySequential ***");
        this.DsnCopy = DsnCopy;
    }

    public ResponseStatus copy(String currDataSet, String[] params) {
        LOG.debug("*** copy ***");
        var fromDataSetName = "";
        var toDataSetName = "";

        final var firstParam = params[1].toUpperCase();
        final var secondParam = params[2].toUpperCase();

        if (Util.isMember(firstParam) && Util.isMember(secondParam)) {
            final var errMSg = "invalid arguments, specify at least one valid sequential dataset, try again...";
            return new ResponseStatus(errMSg, false);
        }

        if (Util.isMember(firstParam) && !Util.isDataSet(secondParam)) {
            final var errMSg = "invalid second argument, specify a valid sequential dataset, try again...";
            return new ResponseStatus(errMSg, false);
        }

        if (!Util.isMember(firstParam) && !Util.isDataSet(firstParam) && Util.isDataSet(secondParam)) {
            final var errMSg = "invalid first argument, specify a valid member or sequential dataset, try again...";
            return new ResponseStatus(errMSg, false);
        }

        if (Util.isMember(secondParam) && !Util.isDataSet(firstParam)) {
            final var errMSg = "invalid first argument, specify a valid sequential dataset, try again...";
            return new ResponseStatus(errMSg, false);
        }

        if (!Util.isMember(secondParam) && !Util.isDataSet(secondParam) && Util.isDataSet(firstParam)) {
            final var errMSg = "invalid second argument, specify a valid member or sequential dataset, try again...";
            return new ResponseStatus(errMSg, false);
        }

        if ((!Util.isMember(firstParam) && !Util.isMember(secondParam)) && (!Util.isDataSet(firstParam) &&
                !Util.isDataSet(secondParam))) {
            return new ResponseStatus(Constants.INVALID_ARGUMENTS, false);
        }

        if (Util.isDataSet(firstParam) && Util.isDataSet(secondParam)) {
            fromDataSetName = firstParam;
            toDataSetName = secondParam;
        } else if (Util.isMember(firstParam) && Util.isDataSet(secondParam)) {
            fromDataSetName = currDataSet + "(" + firstParam + ")";
            toDataSetName = secondParam;
        } else if (Util.isDataSet(firstParam) && Util.isMember(secondParam)) {
            fromDataSetName = firstParam;
            toDataSetName = currDataSet + "(" + secondParam + ")";
        }

        try {
            DsnCopy.copy(fromDataSetName, toDataSetName, true, false);
        } catch (Exception e) {
            if (e.getMessage().contains(Constants.CONNECTION_REFUSED)) {
                return new ResponseStatus(Constants.SEVERE_ERROR, false);
            }
            return new ResponseStatus(e.getMessage(), false);
        }
        return new ResponseStatus("copied to " + toDataSetName, true);
    }

}

