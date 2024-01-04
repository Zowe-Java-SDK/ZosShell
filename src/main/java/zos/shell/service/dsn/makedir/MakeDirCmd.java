package zos.shell.service.dsn.makedir;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosfiles.dsn.input.CreateParams;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCreate;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MakeDirCmd {

    private static final Logger LOG = LoggerFactory.getLogger(MakeDirCmd.class);

    private final DsnCreate dsnCreate;
    private final long timeout;

    public MakeDirCmd(final DsnCreate dsnCreate, long timeout) {
        LOG.debug("*** MakeDirCmd ***");
        this.dsnCreate = dsnCreate;
        this.timeout = timeout;
    }

    public ResponseStatus create(final String dataset, final CreateParams params) {
        LOG.debug("*** create ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureMakeDir(dsnCreate, dataset, params));

        try {
            return submit.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return new ResponseStatus(e.getMessage(), false);
        } finally {
            pool.shutdown();
        }
    }

}
