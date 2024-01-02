package zos.shell.service.dsn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.dto.Member;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnWrite;

public class TouchCmd {

    private static final Logger LOG = LoggerFactory.getLogger(TouchCmd.class);

    private final DsnWrite dsnWrite;
    private final Member members;

    public TouchCmd(DsnWrite dsnWrite, Member members) {
        LOG.debug("*** Touch ***");
        this.dsnWrite = dsnWrite;
        this.members = members;
    }

    public ResponseStatus touch(String dataSet, String member) {
        LOG.debug("*** touch ***");
        if (!Util.isDataSet(dataSet)) {
            return new ResponseStatus(Constants.INVALID_DATASET, true);
        }
        if (!Util.isMember(member)) {
            return new ResponseStatus(Constants.INVALID_MEMBER, true);
        }

        // if member already exist skip write, touch will only create a new member
        var foundExistingMember = false;
        try {
            // TDOD go getMembers in this class remove Member class
            foundExistingMember = members.getMembers(dataSet).stream().anyMatch(m -> m.equalsIgnoreCase(member));
        } catch (ZosmfRequestException ignored) {
        }

        try {
            if (!foundExistingMember) {
                dsnWrite.write(dataSet, member, "");
            } else {
                return new ResponseStatus(member + " already exists.", true);
            }
        } catch (ZosmfRequestException e) {
            final String errMsg = Util.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }

        return new ResponseStatus(member + " successfully created.", true);
    }

}
