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
    private final boolean binary;
    private final long timeoutSeconds;
    private final ExecutorService executor;

    public DownloadSeqDatasetService(final ZosConnection connection,
                                     final PathService pathService,
                                     final boolean binary,
                                     final long timeoutSeconds) {
        this.connection = connection;
        this.pathService = pathService;
        this.binary = binary;
        this.timeoutSeconds = timeoutSeconds;
        this.executor = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
    }

    public List<ResponseStatus> downloadSeqDataset(final String target) {
        LOG.debug("*** downloadSeqDataset ***");

        List<ResponseStatus> results = new ArrayList<>();

        SequentialDatasetCheckResult check = checkSequentialDataset(target);
        if (!check.isValid()) {
            results.add(toFailureStatus(check));
            return results;
        }

        Future<ResponseStatus> future = executor.submit(new FutureDatasetDownload(
                        new DsnGet(connection),
                        pathService,
                        target,
                        binary
                )
        );

        ResponseStatus status = getWithTimeout(future);
        results.add(status);

        if (status.isStatus()) {
            openDownloadedFile(status);
        }

        return results;
    }

    private SequentialDatasetCheckResult checkSequentialDataset(final String target) {
        LOG.debug("*** checkSequentialDataset ***");

        Future<ResponseStatus> future =
                executor.submit(new FutureDatasetInfo(new DsnGet(connection), target));

        try {
            ResponseStatus rs = future.get(timeoutSeconds, TimeUnit.SECONDS);
            boolean isSequential = rs.getMessage().contains("dsorg='PS'");
            return SequentialDatasetCheckResult.valid(isSequential, rs.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            future.cancel(true);
            return SequentialDatasetCheckResult.failure(e);
        } catch (ExecutionException | TimeoutException e) {
            future.cancel(true);
            return SequentialDatasetCheckResult.failure(e);
        }
    }

    private ResponseStatus getWithTimeout(Future<ResponseStatus> future) {
        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            future.cancel(true);
            return new ResponseStatus(e.getCause().getMessage(), false);
        } catch (TimeoutException e) {
            future.cancel(true);
            return new ResponseStatus(Constants.TIMEOUT_MESSAGE, false);
        } catch (ExecutionException e) {
            future.cancel(true);
            return new ResponseStatus(e.getCause().getMessage(), false);
        }
    }

    private ResponseStatus toFailureStatus(final SequentialDatasetCheckResult check) {
        Exception ex = check.getException();

        if (ex instanceof TimeoutException) {
            return new ResponseStatus(Constants.TIMEOUT_MESSAGE, false);
        }

        if (ex != null) {
            return new ResponseStatus(ex.getMessage(), false);
        }

        if (check.getResponseMessage() != null) {
            String msg = check.getResponseMessage() + "\n" + Constants.DOWNLOAD_NOT_SEQ_DATASET_WARNING;
            return new ResponseStatus(msg, false);
        }

        return new ResponseStatus(Constants.DOWNLOAD_NOT_SEQ_DATASET_WARNING, false);
    }

    private void openDownloadedFile(final ResponseStatus status) {
        String path = status.getOptionalData();
        if (path != null) {
            FileUtil.openFileLocation(new File(path).getAbsolutePath());
        }
    }

    @Override
    public void close() {
        LOG.debug("*** close ***");
        executor.shutdown();
    }

    /* ------------------------------------------------------------------ */

    static final class SequentialDatasetCheckResult {

        private final boolean valid;
        private final String responseMessage;
        private final Exception exception;

        private SequentialDatasetCheckResult(boolean valid,
                                             String responseMessage,
                                             Exception exception) {
            this.valid = valid;
            this.responseMessage = responseMessage;
            this.exception = exception;
        }

        static SequentialDatasetCheckResult valid(final boolean isSequential, final String message) {
            return new SequentialDatasetCheckResult(isSequential, message, null);
        }

        static SequentialDatasetCheckResult failure(final Exception e) {
            return new SequentialDatasetCheckResult(false, null, e);
        }

        boolean isValid() {
            return valid;
        }

        String getResponseMessage() {
            return responseMessage;
        }

        Exception getException() {
            return exception;
        }
    }

}
