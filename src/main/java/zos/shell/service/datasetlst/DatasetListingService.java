package zos.shell.service.datasetlst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.model.Dataset;

import java.util.List;
import java.util.concurrent.*;

public class DatasetListingService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetListingService.class);

    private final DsnList dsnList;
    private final long timeout;
    private final ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);

    public DatasetListingService(final DsnList dsnList, final long timeout) {
        LOG.debug("*** DatasetListingService ***");
        this.dsnList = dsnList;
        this.timeout = timeout;
    }

    public List<Dataset> datasetLst(final String dataset) throws ZosmfRequestException {
        LOG.debug("*** datasetLst ***");
        Future<List<Dataset>> future = pool.submit(new FutureDatasetListing(
                dsnList,
                dataset,
                timeout
        ));

        List<Dataset> datasets;
        try {
            datasets = future.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            LOG.debug("Exception dataset listing", e);
            future.cancel(true);
            throw new ZosmfRequestException(getErrorMessage(e));
        } catch (TimeoutException e) {
            LOG.debug("Timeout dataset listing", e);
            future.cancel(true);
            throw new ZosmfRequestException(Constants.TIMEOUT_MESSAGE);
        }

        return datasets;
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
