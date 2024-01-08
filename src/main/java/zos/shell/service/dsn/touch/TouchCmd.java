package zos.shell.service.dsn.touch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.memberlst.MemberLst;
import zos.shell.utility.DsnUtil;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zosfiles.dsn.methods.DsnList;
import zowe.client.sdk.zosfiles.dsn.methods.DsnWrite;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TouchCmd {

    private static final Logger LOG = LoggerFactory.getLogger(TouchCmd.class);

    private final DsnWrite dsnWrite;
    private final DsnList dsnList;
    private final long timeout;

    public TouchCmd(final DsnWrite dsnWrite, final DsnList dsnList, final long timeout) {
        LOG.debug("*** TouchCmd ***");
        this.dsnWrite = dsnWrite;
        this.dsnList = dsnList;
        this.timeout = timeout;
    }

    public ResponseStatus touch(final String dataset, final String target) {
        LOG.debug("*** touch ***");

        if (!DsnUtil.isDataSet(dataset)) {
            return new ResponseStatus(Constants.INVALID_DATASET, true);
        }

        if (!DsnUtil.isMember(target)) {
            return new ResponseStatus(Constants.INVALID_MEMBER, true);
        }

        boolean foundMember;
        MemberLst memberLst = new MemberLst(dsnList, timeout);
        try {
            foundMember = memberLst.memberExist(dataset, target);
        } catch (ZosmfRequestException e) {
            final var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus(errMsg != null ? errMsg : e.getMessage(), false);
        }

        if (foundMember) {
            return new ResponseStatus(target + " already exists", false);
        }

        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureTouch(dsnWrite, dataset, target));

        ResponseStatus responseStatus;
        try {
            responseStatus = submit.get(timeout, TimeUnit.SECONDS);
            return new ResponseStatus(responseStatus.getMessage(), true);
        } catch (InterruptedException | ExecutionException e) {
            LOG.debug("error: " + e);
            submit.cancel(true);
            return new ResponseStatus(e.getMessage() != null && !e.getMessage().isBlank() ?
                    e.getMessage() : Constants.COMMAND_EXECUTION_ERROR_MSG, false);
        } catch (TimeoutException e) {
            submit.cancel(true);
            return new ResponseStatus(Constants.TIMEOUT_MESSAGE, false);
        } finally {
            pool.shutdown();
        }
    }

}
