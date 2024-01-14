package zos.shell.service.dsn.save;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.FutureUtil;
import zowe.client.sdk.zosfiles.dsn.methods.DsnWrite;

import java.util.concurrent.Executors;

public class SaveService {

    private static final Logger LOG = LoggerFactory.getLogger(SaveService.class);

    private final DsnWrite dsnWrite;
    private final long timeout;

    public SaveService(final DsnWrite dsnWrite, final long timeout) {
        LOG.debug("*** SaveService ***");
        this.dsnWrite = dsnWrite;
        this.timeout = timeout;
    }

    public ResponseStatus save(final String dataset, final String target) {
        LOG.debug("*** save ***");
        if (DsnUtil.isMember(target) && !DsnUtil.isDataSet(dataset)) {
            return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false);
        }
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureSave(dsnWrite, dataset, target));
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}

