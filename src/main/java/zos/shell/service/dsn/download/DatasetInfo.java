package zos.shell.service.dsn.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnGet;
import zowe.client.sdk.zosfiles.dsn.model.Dataset;

public class DatasetInfo {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetInfo.class);

    private final DsnGet dsnGet;

    public DatasetInfo(final DsnGet dsnGet) {
        LOG.debug("*** DatasetInfo ***");
        this.dsnGet = dsnGet;
    }

    public ResponseStatus dsInfo(final String dataset) {
        LOG.debug("*** dsInfo ***");

        Dataset response;
        try {
            response = dsnGet.getDsnInfo(dataset);
        } catch (ZosmfRequestException e) {
            var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus(errMsg != null ? errMsg : e.getMessage(), false);
        }

        return new ResponseStatus(response.toString(), true);
    }

}
