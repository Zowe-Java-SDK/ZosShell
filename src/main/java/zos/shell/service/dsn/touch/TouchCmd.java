package zos.shell.service.dsn.touch;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.memberlst.MemberLst;
import zos.shell.utility.ResponseUtil;
import zos.shell.utility.Util;
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

    public ResponseStatus touch(final String dataset, final String member) {
        LOG.debug("*** touch ***");

        if (!Util.isDataSet(dataset)) {
            return new ResponseStatus(Constants.INVALID_DATASET, true);
        }

        if (!Util.isMember(member)) {
            return new ResponseStatus(Constants.INVALID_MEMBER, true);
        }

        final var arrowMsg = Strings.padStart(member, Constants.STRING_PAD_LENGTH, ' ') + Constants.ARROW;

        boolean foundMember;
        MemberLst memberLst = new MemberLst(dsnList, timeout);
        try {
            foundMember = memberLst.memberExist(dataset, member);
        } catch (ZosmfRequestException e) {
            final var errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus(arrowMsg + (errMsg != null ? errMsg : e.getMessage()), false);
        }

        if (foundMember) {
            return new ResponseStatus(arrowMsg + "member already exists", false);
        }

        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureTouch(dsnWrite, dataset, member));

        ResponseStatus responseStatus;
        try {
            responseStatus = submit.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            pool.shutdown();
            submit.cancel(true);
            LOG.debug("error: " + e);
            return new ResponseStatus(arrowMsg + Constants.TIMEOUT_MESSAGE, false);
        }

        pool.shutdown();
        if (responseStatus.isStatus()) {
            return new ResponseStatus(arrowMsg + responseStatus.getMessage(), true);
        } else {
            return new ResponseStatus(arrowMsg + responseStatus.getMessage(), false);
        }
    }

}
