package zos.shell.service.job.tail;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TailCmd {

    private static final Logger LOG = LoggerFactory.getLogger(TailCmd.class);

    private final TextTerminal<?> terminal;
    private final JobGet retrieve;
    private final long timeout;

    public TailCmd(final TextTerminal<?> terminal, final JobGet retrieve, final long timeout) {
        LOG.debug("*** TailCmd ***");
        this.terminal = terminal;
        this.retrieve = retrieve;
        this.timeout = timeout;
    }

    public ResponseStatus tail(String[] params, boolean isAll) {
        LOG.debug("*** tail ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureTail(terminal, retrieve, isAll, timeout, params));

        try {
            return submit.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return new ResponseStatus(e.getMessage(), false);
        } finally {
            pool.shutdown();
        }
    }

}
