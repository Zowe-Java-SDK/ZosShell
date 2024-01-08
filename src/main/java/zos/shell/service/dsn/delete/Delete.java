package zos.shell.service.dsn.delete;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnDelete;

public class Delete {

    private static final Logger LOG = LoggerFactory.getLogger(Delete.class);

    private final DsnDelete dsnDelete;

    public Delete(DsnDelete dsnDelete) {
        LOG.debug("*** Delete constructor ***");
        this.dsnDelete = dsnDelete;
    }

    public ResponseStatus delete(final String dataset, final String member) throws ZosmfRequestException {
        LOG.debug("*** delete member ***");
        try {
            final var response = dsnDelete.delete(dataset, member);
            final var msg = response.getResponsePhrase().orElse("no response phrase").toString();
            return new ResponseStatus(msg, true, member);
        } catch (ZosmfRequestException e) {
            final var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            throw new ZosmfRequestException((errMsg != null ? errMsg : e.getMessage()));
        }
    }

    public ResponseStatus delete(final String dataset) throws ZosmfRequestException {
        LOG.debug("*** delete dataset ***");
        try {
            final var response = dsnDelete.delete(dataset);
            final var msg = response.getResponsePhrase().orElse("no response phrase").toString();
            return new ResponseStatus(msg, true, dataset);
        } catch (ZosmfRequestException e) {
            final var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            throw new ZosmfRequestException((errMsg != null ? errMsg : e.getMessage()));
        }
    }

}
