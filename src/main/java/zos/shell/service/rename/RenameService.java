package zos.shell.service.rename;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.core.ZosConnection;

public class RenameService {

    private static final Logger LOG = LoggerFactory.getLogger(RenameService.class);

    private final ZosConnection connection;
    private final long timeout;

    public RenameService(final ZosConnection connection, final long timeout) {
        LOG.debug("*** CopyService ***");
        this.connection = connection;
        this.timeout = timeout;
    }

    public ResponseStatus rename(final String dataset, final String source, final String destination) {

        return null;
    }

}
