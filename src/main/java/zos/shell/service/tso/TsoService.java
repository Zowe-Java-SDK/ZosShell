package zos.shell.service.tso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.FutureUtil;
import zowe.client.sdk.zostso.method.IssueTso;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TsoService {

    private static final Logger LOG = LoggerFactory.getLogger(TsoService.class);

    private final IssueTso issueTso;
    private final String accountNumber;
    private final long timeout;

    public TsoService(final IssueTso issueTso, final String accountNumber, final long timeout) {
        LOG.debug("*** TsoService ***");
        this.issueTso = issueTso;
        this.accountNumber = accountNumber;
        this.timeout = timeout;
    }

    public ResponseStatus issueCommand(final String command) {
        LOG.debug("*** issueCommand ***");
        if (accountNumber == null || accountNumber.isBlank()) {
            return new ResponseStatus("ACCTNUM is not set, use SET command and try again...", false);
        }
        ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        Future<ResponseStatus> submit = pool.submit(new FutureTso(issueTso, accountNumber, command));
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}
