package zos.shell.service.dsn.touch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.Util;
import zowe.client.sdk.rest.Response;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnWrite;

public class Touch {

    private static final Logger LOG = LoggerFactory.getLogger(Touch.class);

    private final DsnWrite dsnWrite;

    public Touch(DsnWrite dsnWrite) {
        LOG.debug("*** Touch ***");
        this.dsnWrite = dsnWrite;
    }

    public ResponseStatus create(String dataset, String member) {
        LOG.debug("*** Touch ***");
        Response response;
        try {
            response = dsnWrite.write(dataset, member, "");
        } catch (ZosmfRequestException e) {
            final var errMsg = Util.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }

        return new ResponseStatus(response.getResponsePhrase().orElse("no data").toString(), true);
    }

}
