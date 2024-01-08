package zos.shell.service.dsn.delete;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnDelete;

public class Delete {

    private static final Logger LOG = LoggerFactory.getLogger(Delete.class);

    private static final String SUCCESS_MSG = " deleted";
    private final DsnDelete dsnDelete;

    public Delete(DsnDelete dsnDelete) {
        LOG.debug("*** Delete constructor ***");
        this.dsnDelete = dsnDelete;
    }

    public ResponseStatus delete(final String dataset, final String member) throws ZosmfRequestException {
        LOG.debug("*** delete member ***");
        try {
            dsnDelete.delete(dataset, member);
            return new ResponseStatus(member + SUCCESS_MSG, true, member);
        } catch (ZosmfRequestException e) {
            final var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            throw new ZosmfRequestException((errMsg != null ? errMsg : e.getMessage()));
        }
    }

    public ResponseStatus delete(final String dataset) throws ZosmfRequestException {
        LOG.debug("*** delete dataset ***");
        try {
            dsnDelete.delete(dataset);
            return new ResponseStatus(dataset + SUCCESS_MSG, true, dataset);
        } catch (ZosmfRequestException e) {
            final var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            throw new ZosmfRequestException((errMsg != null ? errMsg : e.getMessage()));
        }
    }

}
