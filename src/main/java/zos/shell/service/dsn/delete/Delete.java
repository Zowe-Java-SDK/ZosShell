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

    public ResponseStatus delete(final String dataset, final String member) {
        LOG.debug("*** delete member ***");
        try {
            final var response = dsnDelete.delete(dataset, member);
            if (response.getResponsePhrase().isPresent()) {
                return new ResponseStatus(response.getResponsePhrase().get().toString(), true);
            } else {
                return new ResponseStatus("no response phrase", true);
            }
        } catch (ZosmfRequestException e) {
            final var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }
    }

    public ResponseStatus delete(final String dataset) {
        LOG.debug("*** delete dataset ***");
        try {
            final var response = dsnDelete.delete(dataset);
            if (response.getResponsePhrase().isPresent()) {
                return new ResponseStatus(response.getResponsePhrase().get().toString(), true);
            } else {
                return new ResponseStatus("no response phrase", true);
            }
        } catch (ZosmfRequestException e) {
            final var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }
    }

}
