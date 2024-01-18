package zos.shell.service.dsn.makedir;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.FutureUtil;
import zowe.client.sdk.zosfiles.dsn.input.CreateParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCreate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MakeDirService {

    private static final Logger LOG = LoggerFactory.getLogger(MakeDirService.class);

    private final DsnCreate dsnCreate;
    private final long timeout;

    public MakeDirService(final DsnCreate dsnCreate, long timeout) {
        LOG.debug("*** MakeDirService ***");
        this.dsnCreate = dsnCreate;
        this.timeout = timeout;
    }

    public ResponseStatus create(final String dataset, final CreateParams params) {
        LOG.debug("*** create ***");
        ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        Future<ResponseStatus> submit = pool.submit(new FutureMakeDirectory(dsnCreate, dataset, params));
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}
