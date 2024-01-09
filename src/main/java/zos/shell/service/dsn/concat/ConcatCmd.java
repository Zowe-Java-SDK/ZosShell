package zos.shell.service.dsn.concat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.download.Download;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.FutureUtil;

import java.util.concurrent.Executors;

public class ConcatCmd {

    private static final Logger LOG = LoggerFactory.getLogger(ConcatCmd.class);

    private final Download download;
    private final long timeout;

    public ConcatCmd(final Download download, final long timeout) {
        LOG.debug("*** ConcatCmd ***");
        this.download = download;
        this.timeout = timeout;
    }

    public ResponseStatus cat(final String dataset, final String target) {
        LOG.debug("*** cat ***");
        if (DsnUtil.isMember(target) && !DsnUtil.isDataSet(dataset)) {
            return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false);
        }
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureConcat(download, dataset, target));
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}
