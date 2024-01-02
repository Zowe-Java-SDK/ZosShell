package zos.shell.service.dsn.concatenate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.DownloadCmd;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ConcatCmd {

    private static final Logger LOG = LoggerFactory.getLogger(ConcatCmd.class);

    private final DownloadCmd download;
    private final long timeout;

    public ConcatCmd(DownloadCmd download, long timeout) {
        LOG.debug("*** ConcatCmd ***");
        this.download = download;
        this.timeout = timeout;
    }

    public ResponseStatus cat(final String dataset, final String target) {
        LOG.debug("*** cat ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var future = pool.submit(new FutureConcat(download, dataset, target));

        try {
            final var responseStatus = future.get(timeout, TimeUnit.SECONDS);
            return new ResponseStatus(responseStatus.getMessage(), false);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return new ResponseStatus(e.getMessage(), false);
        }
    }

}
