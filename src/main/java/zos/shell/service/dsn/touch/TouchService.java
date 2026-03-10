package zos.shell.service.dsn.touch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.record.DatasetMember;
import zos.shell.response.ResponseStatus;
import zos.shell.service.memberlst.MemberListingService;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.FutureUtil;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.methods.DsnWrite;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TouchService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(TouchService.class);

    private final DsnWrite dsnWrite;
    private final DsnList dsnList;
    private final long timeout;
    private final ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);

    public TouchService(final DsnWrite dsnWrite, final DsnList dsnList, final long timeout) {
        LOG.debug("*** TouchService ***");
        this.dsnWrite = dsnWrite;
        this.dsnList = dsnList;
        this.timeout = timeout;
    }

    public ResponseStatus touch(String dataset, String target) {
        LOG.debug("*** touch ***");

        DatasetMember datasetMember = DatasetMember.getDatasetAndMember(target);

        if (datasetMember != null) {
            dataset = datasetMember.getDataset();
            target = datasetMember.getMember();
        } else if (DsnUtil.isMember(target) && (dataset == null || dataset.isBlank())) {
            return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false);
        }

        if (dataset == null || dataset.isBlank()) {
            return new ResponseStatus(Constants.DATASET_NOT_SPECIFIED, false);
        }

        try (var memberListingService = new MemberListingService(dsnList, timeout)) {
            if (memberListingService.memberExists(dataset, target)) {
                return new ResponseStatus(target + " already exists", false);
            }
        } catch (ZosmfRequestException e) {
            return buildErrorResponse(e);
        }

        Future<ResponseStatus> future = pool.submit(new FutureTouch(dsnWrite, dataset, target));
        return FutureUtil.waitForResult(future, timeout);
    }

    private ResponseStatus buildErrorResponse(final ZosmfRequestException e) {
        final String errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
        return new ResponseStatus(errMsg != null ? errMsg : e.getMessage(), false);
    }

    @Override
    public void close() {
        pool.shutdown();
    }

}
