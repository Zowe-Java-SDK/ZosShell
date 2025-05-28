package zos.shell.service.dsn.save;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.checksum.CheckSumService;
import zos.shell.service.path.PathService;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.FutureUtil;
import zowe.client.sdk.zosfiles.dsn.methods.DsnWrite;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SaveService {

    private static final Logger LOG = LoggerFactory.getLogger(SaveService.class);

    private final DsnWrite dsnWrite;
    private final PathService pathService;
    private final CheckSumService checkSumService;
    private final long timeout;

    public SaveService(final DsnWrite dsnWrite, final PathService pathService, final CheckSumService checkSumService,
                       final long timeout) {
        LOG.debug("*** SaveService ***");
        this.dsnWrite = dsnWrite;
        this.pathService = pathService;
        this.checkSumService = checkSumService;
        this.timeout = timeout;
    }

    public ResponseStatus save(final String dataset, final String target) {
        LOG.debug("*** save ***");
        if (DsnUtil.isMember(target) && dataset.isBlank()) {
            return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false);
        }
        ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        Future<ResponseStatus> submit = pool.submit(new FutureSave(dsnWrite, pathService, checkSumService, dataset, target));
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}

