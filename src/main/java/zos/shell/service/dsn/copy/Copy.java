package zos.shell.service.dsn.copy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCopy;

public class Copy {

    private static final Logger LOG = LoggerFactory.getLogger(Copy.class);

    private final DsnCopy dsnCopy;

    public Copy(final DsnCopy dsnCopy) {
        LOG.debug("*** Copy ***");
        this.dsnCopy = dsnCopy;
    }

    public ResponseStatus copy(final String source, final String destination, boolean isCopyAll) {
        LOG.debug("*** copy ***");
        try {
            dsnCopy.copy(source, destination, true, isCopyAll);
        } catch (ZosmfRequestException e) {
            final String errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }
        final var msg = isCopyAll ? source + " copied all members to " : source + " copied to " + destination;
        return new ResponseStatus(msg, true, source);
    }

}
