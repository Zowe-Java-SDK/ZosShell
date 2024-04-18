package zos.shell.service.tso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.FutureUtil;
import zos.shell.utility.StrUtil;
import zowe.client.sdk.zostso.method.IssueTso;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TsoService {

    private static final Logger LOG = LoggerFactory.getLogger(TsoService.class);

    private final IssueTso issueTso;
    private final long timeout;

    public TsoService(final IssueTso issueTso, final long timeout) {
        LOG.debug("*** TsoService ***");
        this.issueTso = issueTso;
        this.timeout = timeout;
    }

    public ResponseStatus issueCommand(final String accountNumber, final String command) {
        LOG.debug("*** issueCommand ***");
        if (accountNumber == null || accountNumber.isBlank()) {
            var errMsg = "ACCOUNT_NUMBER is not set, use SET command and try again...";
            return new ResponseStatus(errMsg, false);
        }
        if (!StrUtil.isStrNum(accountNumber)) {
            var errMsg = "ACCOUNT_NUMBER is not a numeric value, use SET command and try again...";
            return new ResponseStatus(errMsg, false);
        }
        ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        Future<ResponseStatus> submit = pool.submit(new FutureTso(issueTso, accountNumber, command));
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}
