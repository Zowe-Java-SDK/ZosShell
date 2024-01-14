package zos.shell.service.datasetlst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Dataset;

import java.util.List;
import java.util.concurrent.*;

public class DatasetListingService {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetListingService.class);

    private final DsnList dsnList;
    private final long timeout;

    private final ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);

    public DatasetListingService(final DsnList dsnList, long timeout) {
        LOG.debug("*** DatasetListingService ***");
        this.dsnList = dsnList;
        this.timeout = timeout;
    }

    public List<Dataset> datasetLst(final String dataset) throws ZosmfRequestException {
        LOG.debug("*** datasetLst ***");
        final var submit = pool.submit(new FutureDatasetListing(dsnList, dataset, timeout));

        List<Dataset> datasets;
        try {
            datasets = submit.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            LOG.debug("error: " + e);
            submit.cancel(true);
            throw new ZosmfRequestException(e.getMessage() != null && !e.getMessage().isBlank() ?
                    e.getMessage() : Constants.COMMAND_EXECUTION_ERROR_MSG);
        } catch (TimeoutException e) {
            submit.cancel(true);
            throw new ZosmfRequestException(Constants.TIMEOUT_MESSAGE);
        } finally {
            pool.shutdown();
        }

        return datasets;
    }

}
