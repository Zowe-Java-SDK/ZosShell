package zos.shell.service.dsn.edit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.checksum.CheckSumService;
import zos.shell.service.dsn.download.Download;
import zos.shell.service.path.PathService;
import zos.shell.utility.DsnUtil;

import java.util.concurrent.*;

public class EditService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(EditService.class);

    private final Download download;
    private final PathService pathService;
    private final CheckSumService checkSumService;
    private final long timeout;
    private final ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);

    public EditService(final Download download, final PathService pathService,
                       final CheckSumService checkSumService, final long timeout) {
        LOG.debug("*** EditService ***");
        this.download = download;
        this.pathService = pathService;
        this.checkSumService = checkSumService;
        this.timeout = timeout;
    }

    public ResponseStatus open(final String dataset, final String target) {
        LOG.debug("Opening editor for dataset '{}' target '{}'", dataset, target);

        if (DsnUtil.isMember(target) && dataset.isBlank()) {
            return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false);
        }

        Future<ResponseStatus> future = pool.submit(new FutureEdit(download,
                pathService,
                checkSumService,
                dataset,
                target
        ));

        try {
            return future.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            LOG.debug("Exception open service", e);
            future.cancel(true);
            return new ResponseStatus(getErrorMessage(e), false);
        } catch (TimeoutException e) {
            LOG.debug("Timeout open service", e);
            future.cancel(true);
            return new ResponseStatus(Constants.TIMEOUT_MESSAGE, false);
        }
    }

    @Override
    public void close() {
        pool.shutdown();
    }

    private String getErrorMessage(final Exception e) {
        LOG.debug("*** getErrorMessage ***");
        return e.getMessage() != null && !e.getMessage().isBlank()
                ? e.getMessage()
                : Constants.COMMAND_EXECUTION_ERROR_MSG;
    }

}
