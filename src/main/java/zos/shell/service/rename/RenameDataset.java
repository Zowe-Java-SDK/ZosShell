package zos.shell.service.rename;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnRename;

public class RenameDataset {

    private static final Logger LOG = LoggerFactory.getLogger(RenameDataset.class);

    private static final String SUCCESS_MSG = " renamed to ";
    private final DsnRename dsnRename;

    public RenameDataset(DsnRename dsnRename) {
        LOG.debug("*** RenameDataset constructor ***");
        this.dsnRename = dsnRename;
    }

    public ResponseStatus renameDataset(final String source, final String destination)
            throws ZosmfRequestException {
        LOG.debug("*** renameDataset ***");
        try {
            dsnRename.dataSetName(source, destination);
            return new ResponseStatus(source + SUCCESS_MSG + destination, true);
        } catch (ZosmfRequestException e) {
            var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            throw new ZosmfRequestException((errMsg != null ? errMsg : e.getMessage()));
        }
    }

}
