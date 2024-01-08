package zos.shell.service.job.tail;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.FutureUtil;
import zowe.client.sdk.zosjobs.methods.JobGet;

import java.util.concurrent.Executors;

public class TailCmd {

    private static final Logger LOG = LoggerFactory.getLogger(TailCmd.class);

    private final TextTerminal<?> terminal;
    private final JobGet retrieve;
    private final long timeout;

    public TailCmd(final TextTerminal<?> terminal, final JobGet retrieve, final long timeout) {
        LOG.debug("*** TailCmd ***");
        this.terminal = terminal;
        this.retrieve = retrieve;
        this.timeout = timeout;
    }

    public ResponseStatus tail(String[] params, boolean isAll) {
        LOG.debug("*** tail ***");

        // example: tail jobOrTaskName 25 all
        if (params.length == 4 && "all".equalsIgnoreCase(params[3])) {
            try {
                Integer.parseInt(params[2]);
            } catch (NumberFormatException e) {
                return new ResponseStatus(Constants.INVALID_PARAMETER, false);
            }
        }

        // example: tail jobOrTaskName 25
        if (params.length == 3 && !"all".equalsIgnoreCase(params[2])) {
            try {
                Integer.parseInt(params[2]);
            } catch (NumberFormatException e) {
                return new ResponseStatus(Constants.INVALID_PARAMETER, false);
            }
        }

        // example: tail jobOrTaskName 25 25
        if (params.length == 4 && !"all".equalsIgnoreCase(params[3])) {
            return new ResponseStatus(Constants.INVALID_PARAMETER, false);
        }

        return doTail(params, isAll);
    }

    private ResponseStatus doTail(String[] params, boolean isAll) {
        final var pool = Executors.newFixedThreadPool(Constants.THREAD_POOL_MIN);
        final var submit = pool.submit(new FutureTail(terminal, retrieve, isAll, timeout, params));
        return FutureUtil.getFutureResponse(submit, pool, timeout);
    }

}
