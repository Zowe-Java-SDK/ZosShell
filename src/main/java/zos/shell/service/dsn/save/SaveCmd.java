package zos.shell.service.dsn.save;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosfiles.dsn.methods.DsnWrite;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SaveCmd {

    private static final Logger LOG = LoggerFactory.getLogger(SaveCmd.class);

    private final DsnWrite dsnWrite;
    private final long timeout;

    public SaveCmd(final DsnWrite dsnWrite, final long timeout) {
        LOG.debug("*** SaveCmd ***");
        this.dsnWrite = dsnWrite;
        this.timeout = timeout;
    }

    public ResponseStatus save(final String dataSet, final String target) {
        LOG.debug("*** save ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureSave(dsnWrite, dataSet, target));

        try {
            return submit.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            LOG.debug("error: " + e);
            submit.cancel(true);
            return new ResponseStatus(e.getMessage() != null && !e.getMessage().isBlank() ?
                    e.getMessage() : Constants.COMMAND_EXECUTION_ERROR_MSG, false);
        } catch (TimeoutException e) {
            submit.cancel(true);
            return new ResponseStatus(Constants.TIMEOUT_MESSAGE, false);
        } finally {
            pool.shutdown();
        }
    }

}

