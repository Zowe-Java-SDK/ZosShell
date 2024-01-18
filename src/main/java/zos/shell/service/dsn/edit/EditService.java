package zos.shell.service.dsn.edit;

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

public class EditService {

    private static final Logger LOG = LoggerFactory.getLogger(EditService.class);

    private final Download download;
    private final long timeout;

    public EditService(final Download download, long timeout) {
        LOG.debug("*** EditService ***");
        this.download = download;
        this.timeout = timeout;
    }

    public ResponseStatus open(final String dataset, final String target) {
        LOG.debug("*** open ***");
        if (DsnUtil.isMember(target) && !DsnUtil.isDataSet(dataset)) {
            return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false);
        }
        ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        Future<ResponseStatus> submit = pool.submit(new FutureEdit(download, dataset, target));
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}
