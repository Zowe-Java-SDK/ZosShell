package zos.shell.service.uname;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.console.FutureConsole;
import zos.shell.utility.FutureUtil;
import zowe.client.sdk.zosconsole.method.ConsoleCmd;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UnameService {

    private static final Logger LOG = LoggerFactory.getLogger(UnameService.class);

    private final ConsoleCmd issueConsole;
    private final long timeout;

    public UnameService(final ConsoleCmd issueConsole, final long timeout) {
        LOG.debug("*** UnameService ***");
        this.issueConsole = issueConsole;
        this.timeout = timeout;
    }

    public String getUname(final String hostName, final String consoleName) {
        LOG.debug("*** getUname ***");

        ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        Future<ResponseStatus> submit = pool.submit(new FutureConsole(issueConsole, consoleName, "D IPLINFO"));
        ResponseStatus responseStatus = FutureUtil.getFutureResponse(submit, pool, timeout);

        if (!responseStatus.isStatus()) {
            return Constants.NO_INFO;
        }
        var output = responseStatus.getMessage();
        int index = output.indexOf("RELEASE z/OS ");
        String zosVersion = null;
        if (index >= 0) {
            zosVersion = output.substring(index, index + 22);
        }
        if (zosVersion == null) {
            return Constants.NO_INFO;
        }
        return "hostname: " + hostName + ", " + zosVersion;
    }

}
