package zos.shell.service.job.terminate;

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

public class TerminateService {

    private static final Logger LOG = LoggerFactory.getLogger(TerminateService.class);

    private final ConsoleCmd issueConsole;
    private final long timeout;

    public enum Type {
        STOP,
        CANCEL
    }

    public TerminateService(final ConsoleCmd issueConsole, final long timeout) {
        LOG.debug("*** TerminateService ***");
        this.issueConsole = issueConsole;
        this.timeout = timeout;
    }

    public ResponseStatus terminate(final TerminateService.Type type, final String consoleName, final String target) {
        LOG.debug("*** terminate ***");
        String command;
        switch (type) {
            case STOP:
                command = "P " + target;
                break;
            case CANCEL:
                command = "C " + target;
                break;
            default:
                return new ResponseStatus("invalid termination type, try again...", false);
        }

        ExecutorService pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        Future<ResponseStatus> submit = pool.submit(new FutureConsole(issueConsole, consoleName, command));
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}
