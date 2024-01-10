package zos.shell.service.job.terminate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.service.console.FutureMvs;
import zos.shell.utility.FutureUtil;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.zosconsole.method.IssueConsole;

import java.util.concurrent.Executors;

public class TerminateCmd {

    private static final Logger LOG = LoggerFactory.getLogger(TerminateCmd.class);

    private final IssueConsole issueConsole;
    private final ZosConnection connection;
    private final long timeout;

    public enum Type {
        STOP,
        CANCEL
    }

    public TerminateCmd(final ZosConnection connection, final IssueConsole issueConsole, final long timeout) {
        LOG.debug("*** Terminate ***");
        this.connection = connection;
        this.issueConsole = issueConsole;
        this.timeout = timeout;
    }

    public ResponseStatus terminate(final TerminateCmd.Type type, final String target) {
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

        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureMvs(connection, issueConsole, command));
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}
