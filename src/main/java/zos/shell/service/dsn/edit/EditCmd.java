package zos.shell.service.dsn.edit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.DownloadCmd;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EditCmd {

    private static final Logger LOG = LoggerFactory.getLogger(EditCmd.class);

    private final DownloadCmd download;
    private final long timeout;

    public EditCmd(final DownloadCmd download, long timeout) {
        LOG.debug("*** EditCmd ***");
        this.download = download;
        this.timeout = timeout;
    }

    public ResponseStatus open(final String dataset, final String target) {
        LOG.debug("*** open ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureEdit(download, dataset, target));

        try {
            return submit.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return new ResponseStatus(e.getMessage(), false);
        } finally {
            pool.shutdown();
        }
    }

}
