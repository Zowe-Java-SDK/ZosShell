package zos.shell.service.tso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.FutureUtil;
import zowe.client.sdk.zostso.method.IssueTso;

import java.util.concurrent.Executors;

public class TsoCmd {

    private static final Logger LOG = LoggerFactory.getLogger(TsoCmd.class);

    private final IssueTso issueTso;
    private final String accountNumber;
    private final long timeout;

    public TsoCmd(final IssueTso issueTso, final String accountNumber, final long timeout) {
        LOG.debug("*** TsoCmd ***");
        this.issueTso = issueTso;
        this.accountNumber = accountNumber;
        this.timeout = timeout;
    }

    public ResponseStatus issueCommand(final String command) {
        LOG.debug("*** issueCommand ***");
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureTso(issueTso, accountNumber, command));
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}
