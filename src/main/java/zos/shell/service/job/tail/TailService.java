package zos.shell.service.job.tail;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.FutureUtil;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TailService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(TailService.class);

    private final TextTerminal<?> terminal;
    private final JobGet retrieve;
    private final long timeout;
    private final ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);

    public TailService(final TextTerminal<?> terminal, final JobGet retrieve, final long timeout) {
        LOG.debug("*** TailService ***");
        this.terminal = terminal;
        this.retrieve = retrieve;
        this.timeout = timeout;
    }

    public ResponseStatus tail(String target, int lines) {
        LOG.debug("*** tail ***");
        Future<ResponseStatus> future = pool.submit(new FutureTail(
                terminal,
                retrieve,
                lines,
                timeout,
                target
        ));
        return FutureUtil.getResponseStatus(future, timeout);
    }

    @Override
    public void close() {
        LOG.debug("*** close ***");
        pool.shutdown();
    }

}
