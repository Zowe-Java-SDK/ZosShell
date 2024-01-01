package zos.shell.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.dto.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.input.CreateParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCreate;

public class MakeDirectory {

    private static final Logger LOG = LoggerFactory.getLogger(MakeDirectory.class);

    private final DsnCreate dsnCreate;

    public MakeDirectory(DsnCreate dsnCreate) {
        LOG.debug("*** MakeDirectory ***");
        this.dsnCreate = dsnCreate;
    }

    public ResponseStatus mkdir(String dataset, CreateParams params) {
        LOG.debug("*** mkdir ***");
        try {
            dsnCreate.create(dataset, params);
        } catch (ZosmfRequestException e) {
            final String errMsg = Util.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }

        return new ResponseStatus(dataset + " created successfully...", true);
    }

}
