package zos.shell.service.dsn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.input.CreateParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCreate;

public class MakeDirCmd {

    private static final Logger LOG = LoggerFactory.getLogger(MakeDirCmd.class);

    private final DsnCreate dsnCreate;

    public MakeDirCmd(DsnCreate dsnCreate) {
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
