package zos.shell.utility;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;

import java.util.List;
import java.util.concurrent.*;

public final class FutureUtil {

    private static final Logger LOG = LoggerFactory.getLogger(FutureUtil.class);

    public static ResponseStatus getFutureResponse(final Future<ResponseStatus> future, final ExecutorService pool,
                                                   final long timeout) {
        LOG.debug("*** getFutureResponse ***");
        try {
            return future.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            LOG.debug("error: " + e);
            future.cancel(true);
            boolean isErrMsg = e.getMessage() != null && !e.getMessage().isBlank();
            var errMsg = isErrMsg ? e.getMessage() : Constants.COMMAND_EXECUTION_ERROR_MSG;
            return new ResponseStatus(errMsg, false);
        } catch (TimeoutException e) {
            future.cancel(true);
            return new ResponseStatus(Constants.TIMEOUT_MESSAGE, false);
        } finally {
            pool.shutdown();
        }
    }

    public static ResponseStatus getFutureResponses(final List<Future<ResponseStatus>> futures,
                                                    final ExecutorService pool, final long timeout,
                                                    final int padLength) {
        LOG.debug("*** getFutureResponses ***");
        var results = new StringBuilder();
        futures.forEach(future -> {
            ResponseStatus responseStatus;
            try {
                responseStatus = future.get(timeout, TimeUnit.SECONDS);
                var arrowMsg = Strings.padStart(responseStatus.getOptionalData(), padLength, ' ');
                arrowMsg += Constants.ARROW;
                results.append(arrowMsg).append(responseStatus.getMessage()).append("\n");
            } catch (InterruptedException | ExecutionException e) {
                LOG.debug("error: " + e);
                future.cancel(true);
                var isErrMsg = e.getMessage() != null && !e.getMessage().isBlank();
                var errMsg = isErrMsg ? e.getMessage() : Constants.COMMAND_EXECUTION_ERROR_MSG;
                results.append(errMsg).append("\n");
            } catch (TimeoutException e) {
                future.cancel(true);
                results.append(Constants.TIMEOUT_MESSAGE).append("\n");
            }
        });

        pool.shutdown();
        return new ResponseStatus(results.toString(), true);
    }

}
