package zos.shell.utility;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.rest.exception.ZosmfRequestException;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class FutureUtil {

    private static final Logger LOG = LoggerFactory.getLogger(FutureUtil.class);

    private FutureUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static ResponseStatus getResponseStatus(final Future<ResponseStatus> future, final long timeout) {
        LOG.debug("*** getResponseStatus ***");
        try {
            return future.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            return new ResponseStatus(getErrorMessage(e), false);
        } catch (ExecutionException e) {
            future.cancel(true);
            return new ResponseStatus(getErrorMessage(e), false);
        } catch (TimeoutException e) {
            future.cancel(true);
            return new ResponseStatus(Constants.TIMEOUT_MESSAGE, false);
        }
    }

    public static <T> T getFutureValue(final Future<T> future, final long timeout)
            throws ZosmfRequestException {
        LOG.debug("*** getFutureValue ***");
        try {
            return future.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new ZosmfRequestException(getErrorMessage(e));
        } catch (ExecutionException e) {
            future.cancel(true);
            Throwable cause = e.getCause();
            if (cause instanceof ZosmfRequestException) {
                throw (ZosmfRequestException) cause;
            }
            throw new ZosmfRequestException(getErrorMessage(e));
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new ZosmfRequestException(Constants.TIMEOUT_MESSAGE);
        }
    }

    public static String getErrorMessage(final Exception e) {
        LOG.debug("*** getErrorMessage ***");
        return e.getMessage() != null && !e.getMessage().isBlank()
                ? e.getMessage()
                : Constants.COMMAND_EXECUTION_ERROR_MSG;
    }

    public static ResponseStatus collectFutureResponses(final List<Future<ResponseStatus>> futures,
                                                        final long timeout,
                                                        final int padLength) {
        LOG.debug("*** collectFutureResponses ***");
        var results = new StringBuilder();

        for (Future<ResponseStatus> future : futures) {
            try {
                ResponseStatus responseStatus = future.get(timeout, TimeUnit.SECONDS);
                var arrowMsg = Strings.padStart(responseStatus.getOptionalData(), padLength, ' ');
                arrowMsg += Constants.ARROW;
                results.append(arrowMsg).append(responseStatus.getMessage()).append("\n");
            } catch (InterruptedException e) {
                future.cancel(true);
                Thread.currentThread().interrupt();
                results.append(getErrorMessage(e)).append("\n");
                break;
            } catch (ExecutionException e) {
                future.cancel(true);
                results.append(getErrorMessage(e)).append("\n");
            } catch (TimeoutException e) {
                future.cancel(true);
                results.append(Constants.TIMEOUT_MESSAGE).append("\n");
            }
        }

        return new ResponseStatus(results.toString(), true);
    }

}
