package zos.shell.service.rename;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.FutureUtil;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosfiles.dsn.methods.DsnRename;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RenameService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(RenameService.class);

    private final ZosConnection connection;
    private final long timeout;
    private final ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);

    public RenameService(final ZosConnection connection, final long timeout) {
        LOG.debug("*** RenameService ***");
        this.connection = connection;
        this.timeout = timeout;
    }

    public ResponseStatus rename(final String dataset, final String source, final String destination) {
        LOG.debug("*** rename ***");

        final boolean isMember = DsnUtil.isMember(source);
        final boolean isDataSet = DsnUtil.isDataset(source);
        final Future<ResponseStatus> future;

        if (isMember && !DsnUtil.isMember(destination)) {
            return new ResponseStatus(Constants.INVALID_MEMBER, false);
        }

        if (isDataSet && !DsnUtil.isDataset(destination)) {
            return new ResponseStatus(Constants.INVALID_DATASET, false);
        }

        if (isMember) {
            if (dataset == null || dataset.isBlank()) {
                return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false);
            }

            future = pool.submit(new FutureRenameMember(
                    new DsnRename(connection),
                    dataset,
                    source,
                    destination
            ));
        } else if (isDataSet) {
            future = pool.submit(new FutureRenameDataset(
                    new DsnRename(connection),
                    source,
                    destination
            ));
        } else {
            return new ResponseStatus(Constants.INVALID_COMMAND, false);
        }

        return FutureUtil.getResponseStatus(future, timeout);
    }

    @Override
    public void close() {
        LOG.debug("*** close ***");
        pool.shutdown();
    }

}
