package zos.shell.service.datasetlst;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.response.Dataset;

import java.util.List;
import java.util.concurrent.*;

public class DatasetLst {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetLst.class);

    private final DsnList dsnList;
    private final long timeout;

    private final ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);

    public DatasetLst(final DsnList dsnList, long timeout) {
        LOG.debug("*** DatasetLst ***");
        this.dsnList = dsnList;
        this.timeout = timeout;
    }

    public List<Dataset> datasetLst(final String dataset) throws ZosmfRequestException {
        LOG.debug("*** datasetLst ***");
        final var submit = pool.submit(new FutureDatasetLst(dsnList, dataset));

        List<Dataset> datasets;
        try {
            datasets = submit.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new ZosmfRequestException(e.getMessage());
        } finally {
            pool.shutdown();
        }

        return datasets;
    }

}
