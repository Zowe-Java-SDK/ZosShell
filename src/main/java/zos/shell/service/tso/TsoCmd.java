package zos.shell.service.tso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zostso.method.IssueTso;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TsoCmd {

    private static final Logger LOG = LoggerFactory.getLogger(TsoCmd.class);

    private final IssueTso issueTso;
    private final String accountNumber;
    private final long timeout;

    public TsoCmd(final IssueTso issueTso, final String accountNumber, final long timeout) {
        LOG.debug("*** TsoCmd ***");
        this.issueTso = issueTso;
        this.accountNumber = accountNumber;
        this.timeout = timeout;
    }

    public ResponseStatus issueCommand(final String command) {
        LOG.debug("*** issueCommand ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureTso(issueTso, accountNumber, command));

        try {
            return submit.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            submit.cancel(true);
            LOG.debug("error: " + e);
            return new ResponseStatus(Constants.TIMEOUT_MESSAGE, false);
        } finally {
            pool.shutdown();
        }
    }

}
