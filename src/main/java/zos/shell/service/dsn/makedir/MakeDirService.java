package zos.shell.service.dsn.makedir;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.FutureUtil;
import zowe.client.sdk.zosfiles.dsn.input.CreateParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCreate;

import java.util.concurrent.Executors;

public class MakeDirService {

    private static final Logger LOG = LoggerFactory.getLogger(MakeDirService.class);

    private final DsnCreate dsnCreate;
    private final long timeout;

    public MakeDirService(final DsnCreate dsnCreate, long timeout) {
        LOG.debug("*** MakeDirectoryService ***");
        this.dsnCreate = dsnCreate;
        this.timeout = timeout;
    }

    public ResponseStatus create(final String dataset, final CreateParams params) {
        LOG.debug("*** create ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureMakeDirectory(dsnCreate, dataset, params));
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}
