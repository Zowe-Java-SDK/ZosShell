package zos.shell.service.dsn.concat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.download.Download;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.FutureUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ConcatService {

    private static final Logger LOG = LoggerFactory.getLogger(ConcatService.class);

    private final Download download;
    private final long timeout;

    public ConcatService(final Download download, final long timeout) {
        LOG.debug("*** ConcatService ***");
        this.download = download;
        this.timeout = timeout;
    }

    public ResponseStatus cat(final String dataset, final String target) {
        LOG.debug("*** cat ***");
        if (DsnUtil.isMember(target) && dataset.isBlank()) {
            return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false);
        }
        ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        Future<ResponseStatus> submit = pool.submit(new FutureConcat(download, dataset, target));
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}
