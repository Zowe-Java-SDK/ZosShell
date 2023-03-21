package zos.shell.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.dto.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.zosfiles.ZosDsn;
import zowe.client.sdk.zosfiles.input.CreateParams;

public class MakeDirectory {

    private static Logger LOG = LoggerFactory.getLogger(MakeDirectory.class);

    private final ZosDsn zosDsn;

    public MakeDirectory(ZosDsn zosDsn) {
        LOG.debug("*** MakeDirectory ***");
        this.zosDsn = zosDsn;
    }

    public ResponseStatus mkdir(String dataset, CreateParams params) {
        LOG.debug("*** mkdir ***");
        try {
            zosDsn.createDsn(dataset, params);
        } catch (Exception e) {
            return new ResponseStatus(Util.getErrorMsg(e + ""), false);
        }

        return new ResponseStatus(dataset + " created successfully...", true);
    }

}
