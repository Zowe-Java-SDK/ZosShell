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

public class UnameService implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(UnameService.class);
    private static final String IPLINFO_COMMAND = "D IPLINFO";
    private static final String RELEASE_PREFIX = "RELEASE z/OS ";
    private static final int RELEASE_LENGTH = 22;

    private final ConsoleCmd issueConsole;
    private final long timeout;
    private final ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);

    public UnameService(final ConsoleCmd issueConsole, final long timeout) {
        LOG.debug("*** UnameService ***");
        this.issueConsole = issueConsole;
        this.timeout = timeout;
    }

    public String getUname(final String hostName, final String consoleName) {
        LOG.debug("*** getUname ***");

        Future<ResponseStatus> future = pool.submit(new FutureConsole(
                issueConsole,
                consoleName,
                IPLINFO_COMMAND
        ));

        ResponseStatus responseStatus = FutureUtil.getResponseStatus(future, timeout);
        if (!responseStatus.isStatus()) {
            return Constants.NO_INFO;
        }

        String output = responseStatus.getMessage();
        if (output == null || output.isBlank()) {
            return Constants.NO_INFO;
        }

        int index = output.indexOf(RELEASE_PREFIX);
        if (index < 0) {
            return Constants.NO_INFO;
        }

        int endIndex = Math.min(output.length(), index + RELEASE_LENGTH);
        String zosVersion = output.substring(index, endIndex);

        return "hostname: " + hostName + ", " + zosVersion;
    }

    @Override
    public void close() {
        LOG.debug("*** close ***");
        pool.shutdown();
    }

}
