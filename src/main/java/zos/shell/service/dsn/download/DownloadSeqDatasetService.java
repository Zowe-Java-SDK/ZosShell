package zos.shell.service.dsn.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.path.PathService;
import zos.shell.utility.FileUtil;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosfiles.dsn.methods.DsnGet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class DownloadSeqDatasetService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadSeqDatasetService.class);

    private final ZosConnection connection;
    private final PathService pathService;
    private final boolean isBinary;
    private final long timeout;
    private final ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);

    public static class SequentialDatasetCheckResult {

        private final boolean isValid;
        private final Exception exception;

        public SequentialDatasetCheckResult(boolean isValid, Exception exception) {
            this.isValid = isValid;
            this.exception = exception;
        }

        public boolean isValid() {
            return isValid;
        }

        public Exception getException() {
            return exception;
        }
    }

    public DownloadSeqDatasetService(final ZosConnection connection, final PathService pathService,
                                     final boolean isBinary, final long timeout) {
        LOG.debug("*** DownloadSeqDatasetService ***");
        this.connection = connection;
        this.pathService = pathService;
        this.isBinary = isBinary;
        this.timeout = timeout;
    }

    public List<ResponseStatus> downloadSeqDataset(final String target) {
        LOG.debug("*** downloadSeqDataset ***");
        List<ResponseStatus> results = new ArrayList<>();

        SequentialDatasetCheckResult result = checkSequentialDataset(target);
        if (!result.isValid()) {
            Exception exception = result.getException();
            if (exception instanceof TimeoutException) {
                results.add(new ResponseStatus(Constants.TIMEOUT_MESSAGE, false));
            } else if (exception != null) {
                results.add(new ResponseStatus(getErrorMessage(exception), false));
            } else {
                results.add(new ResponseStatus(Constants.DOWNLOAD_NOT_SEQ_DATASET_WARNING, false));
            }
            return results;
        }

        Future<ResponseStatus> future = pool.submit(new FutureDatasetDownload(
                new DsnGet(connection),
                pathService,
                target,
                isBinary
        ));
        try {
            ResponseStatus status = future.get(timeout, TimeUnit.SECONDS);
            results.add(status);
            if (status.isStatus()) {
                FileUtil.openFileLocation(new File(status.getOptionalData()).getAbsolutePath());
            }
        } catch (TimeoutException e) {
            future.cancel(true);
            results.add(new ResponseStatus(Constants.TIMEOUT_MESSAGE, false));
        } catch (Exception e) {
            future.cancel(true);
            results.add(new ResponseStatus(getErrorMessage(e), false));
        }

        return results;
    }

    @Override
    public void close() {
        pool.shutdown();
    }

    private SequentialDatasetCheckResult checkSequentialDataset(final String target) {
        LOG.debug("*** checkSequentialDataset ***");
        try {
            Future<ResponseStatus> future = pool.submit(new FutureDatasetInfo(new DsnGet(connection), target));
            ResponseStatus responseStatus = future.get(timeout, TimeUnit.SECONDS);
            boolean isSequential = responseStatus.getMessage().contains("dsorg='PS'");
            return new SequentialDatasetCheckResult(isSequential, null);
        } catch (TimeoutException e) {
            LOG.debug("Timeout checking dataset", e);
            return new SequentialDatasetCheckResult(false, e);
        } catch (ExecutionException | InterruptedException e) {
            LOG.debug("Error checking dataset", e);
            return new SequentialDatasetCheckResult(false, e);
        }
    }

    private String getErrorMessage(final Exception e) {
        LOG.debug("*** getErrorMessage ***");
        return e.getMessage() != null && !e.getMessage().isBlank()
                ? e.getMessage()
                : Constants.COMMAND_EXECUTION_ERROR_MSG;
    }

}
