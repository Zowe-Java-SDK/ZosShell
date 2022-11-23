package zos.shell.commands;

import zos.shell.Constants;
import zos.shell.dto.Member;
import zos.shell.dto.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.zosfiles.ZosDsn;

public class Touch {

    private final ZosDsn zosDsn;
    private final Member members;

    public Touch(ZosDsn zosDsn, Member members) {
        this.zosDsn = zosDsn;
        this.members = members;
    }

    public ResponseStatus touch(String dataSet, String member) {
        if (!Util.isDataSet(dataSet)) {
            return new ResponseStatus(Constants.INVALID_DATASET, true);
        }
        if (!Util.isMember(member)) {
            return new ResponseStatus(Constants.INVALID_MEMBER, true);
        }

        // if member already exist skip write, touch will only create a new member
        var foundExistingMember = false;
        try {
            foundExistingMember = members.getMembers(dataSet).stream().anyMatch(m -> m.equalsIgnoreCase(member));
        } catch (Exception ignored) {
        }

        try {
            if (!foundExistingMember) {
                zosDsn.writeDsn(dataSet, member, "");
            } else {
                return new ResponseStatus(member + " already exists.", true);
            }
        } catch (Exception e) {
            return new ResponseStatus(Util.getErrorMsg(e + ""), true);
        }

        return new ResponseStatus(member + " successfully created.", true);
    }

}
