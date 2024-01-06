package zos.shell.service.dsn.touch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.rest.Response;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnWrite;

public class Touch {

    private static final Logger LOG = LoggerFactory.getLogger(Touch.class);

    private final DsnWrite dsnWrite;

    public Touch(final DsnWrite dsnWrite) {
        LOG.debug("*** Touch ***");
        this.dsnWrite = dsnWrite;
    }

    public ResponseStatus create(final String dataset, final String member) {
        LOG.debug("*** create ***");
        Response response;
        try {
            response = dsnWrite.write(dataset, member, "");
        } catch (ZosmfRequestException e) {
            final var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }

        return new ResponseStatus(response.getResponsePhrase().orElse("no data").toString(), true);
    }

}
