package zos.shell.service.dsn.makedir;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.input.CreateParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCreate;

public class MakeDir {

    private static final Logger LOG = LoggerFactory.getLogger(MakeDir.class);

    private final DsnCreate dsnCreate;

    public MakeDir(final DsnCreate dsnCreate) {
        LOG.debug("*** MakeDir ***");
        this.dsnCreate = dsnCreate;
    }

    public ResponseStatus create(final String dataset, final CreateParams params) {
        LOG.debug("*** create ***");

        try {
            dsnCreate.create(dataset, params);
        } catch (ZosmfRequestException e) {
            final String errMsg = Util.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }

        return new ResponseStatus(dataset + " created successfully...", true);
    }

}
