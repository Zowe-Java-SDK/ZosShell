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

public class RenameService {

    private static final Logger LOG = LoggerFactory.getLogger(RenameService.class);

    private final ZosConnection connection;
    private final long timeout;

    public RenameService(final ZosConnection connection, final long timeout) {
        LOG.debug("*** RenameService ***");
        this.connection = connection;
        this.timeout = timeout;
    }

    public ResponseStatus rename(final String dataset, final String source, final String destination) {
        LOG.debug("*** rename ***");

        boolean isMember = DsnUtil.isMember(source);
        boolean isDataSet = DsnUtil.isDataset(source);
        
        if (isMember && !DsnUtil.isMember(destination)) {
            return new ResponseStatus(Constants.INVALID_MEMBER, false);
        }

        if (isDataSet && !DsnUtil.isDataset(destination)) {
            return new ResponseStatus(Constants.INVALID_DATASET, false);
        }

        ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        Future<ResponseStatus> submit;

        if (isMember) {
            submit = pool.submit(new FutureRenameMember(new DsnRename(connection), dataset, source, destination));
            return FutureUtil.getFutureResponse(submit, pool, timeout);
        } else if (isDataSet) {
            submit = pool.submit(new FutureRenameDataset(new DsnRename(connection), source, destination));
            return FutureUtil.getFutureResponse(submit, pool, timeout);
        } else {
            return new ResponseStatus(Constants.INVALID_COMMAND, false);
        }
    }

}
