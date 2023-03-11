package zos.shell.commands;

import zos.shell.dto.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.zosfiles.ZosDsn;
import zowe.client.sdk.zosfiles.input.CreateParams;

public class MakeDirectory {

    private final ZosDsn zosDsn;

    public MakeDirectory(ZosDsn zosDsn) {
        this.zosDsn = zosDsn;
    }

    public ResponseStatus mkdir(String dataset, CreateParams params) {
        try {
            zosDsn.createDsn(dataset, params);
        } catch (Exception e) {
            return new ResponseStatus(Util.getErrorMsg(e + ""), false);
        }

        return new ResponseStatus(dataset + " created successfully...", true);
    }

}
