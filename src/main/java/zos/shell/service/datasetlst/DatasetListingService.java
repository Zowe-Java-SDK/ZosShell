package zos.shell.service.datasetlst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.utility.FutureUtil;
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

    public List<Dataset> listDatasets(final String dataset) throws ZosmfRequestException {
        LOG.debug("*** listDatasets ***");
        Future<List<Dataset>> future = pool.submit(new FutureDatasetListing(
                dsnList,
                dataset,
                timeout
        ));

        //noinspection DuplicatedCode
        try {
            return future.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            future.cancel(true);
            throw new ZosmfRequestException(FutureUtil.getErrorMessage(e));
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new ZosmfRequestException(Constants.TIMEOUT_MESSAGE);
        }
    }

    @Override
    public void close() {
        pool.shutdown();
    }

}
