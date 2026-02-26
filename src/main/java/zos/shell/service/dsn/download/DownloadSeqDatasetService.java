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

public class DownloadSeqDatasetService {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadSeqDatasetService.class);

    private final ZosConnection connection;
    private final PathService pathService;
    private final boolean isBinary;
    private final long timeout;

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
        ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        Future<ResponseStatus> submit = null;

        try {
            if (!isSeqDataset(target)) {
                results.add(new ResponseStatus(Constants.DOWNLOAD_NOT_SEQ_DATASET_WARNING, false));
                return results;
            }
        } catch (ExecutionException | InterruptedException e) {
            LOG.debug(String.valueOf(e));
            results.add(new ResponseStatus(e.getMessage() != null && !e.getMessage().isBlank() ?
                    e.getMessage() : Constants.COMMAND_EXECUTION_ERROR_MSG, false));
            return results;
        } catch (TimeoutException e) {
            results.add(new ResponseStatus(Constants.TIMEOUT_MESSAGE, false));
            return results;
        }

        try {
            submit = pool.submit(new FutureDatasetDownload(new DsnGet(connection), pathService, target, isBinary));
            results.add(submit.get(timeout, TimeUnit.SECONDS));
        } catch (InterruptedException | ExecutionException e) {
            LOG.debug(String.valueOf(e));
            submit.cancel(true);
            results.add(new ResponseStatus(e.getMessage() != null && !e.getMessage().isBlank() ?
                    e.getMessage() : Constants.COMMAND_EXECUTION_ERROR_MSG, false));
        } catch (TimeoutException e) {
            submit.cancel(true);
            results.add(new ResponseStatus(Constants.TIMEOUT_MESSAGE, false));
        } finally {
            pool.shutdown();
        }

        if (results.get(0).isStatus()) {
            var file = new File(results.get(0).getOptionalData());
            FileUtil.openFileLocation(file.getAbsolutePath());
            return results;
        }

        return results;
    }

    private boolean isSeqDataset(String target) throws ExecutionException, InterruptedException, TimeoutException {
        LOG.debug("*** isSeqDataset ***");

        ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        ResponseStatus responseStatus;
        Future<ResponseStatus> submit;

        try {
            submit = pool.submit(new FutureDatasetInfo(new DsnGet(connection), target));
            responseStatus = submit.get(timeout, TimeUnit.SECONDS);
        } finally {
            pool.shutdown();
        }

        return responseStatus.getMessage().contains("dsorg='PS'");
    }

}
